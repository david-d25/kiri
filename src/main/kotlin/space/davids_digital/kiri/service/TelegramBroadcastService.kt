package space.davids_digital.kiri.service

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import space.davids_digital.kiri.integration.telegram.TelegramHtmlMapper
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.TelegramBroadcast
import space.davids_digital.kiri.model.TelegramBroadcast.Status
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.service.exception.ValidationException
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

/**
 * Sends one message to every enabled Telegram chat.
 *
 * A run is started in the background and its progress is polled via [findLatest], so that neither a slow
 * delivery nor a lost HTTP connection can interrupt it. A chat that rejects the message (bot blocked by the
 * user, bot kicked from the group, chat deleted) is recorded as a failure and the run continues with the
 * next chat.
 */
@Service
class TelegramBroadcastService(
    private val chatOrm: TelegramChatOrmService,
    private val telegram: TelegramService,
) {
    companion object {
        private const val CHATS_PAGE_SIZE = 200
        private const val MAX_STORED_RUNS = 10

        /** Failures beyond this are still counted, just not kept in memory. */
        private const val MAX_STORED_FAILURES = 200

        /**
         * Per-chat deadline. Telegram sends retry 429s in a loop without a bound of its own, so without this
         * a single unlucky chat could stall the whole run and block every later broadcast.
         */
        private val DELIVERY_TIMEOUT = 1.minutes
    }

    private val log = LoggerFactory.getLogger(this::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val runs = ConcurrentHashMap<UUID, TelegramBroadcast>()
    private val startLock = Any()

    @Volatile
    private var latestId: UUID? = null

    fun countRecipients(): Long = chatOrm.countAllEnabled()

    fun findById(id: UUID): TelegramBroadcast? = runs[id]

    fun findLatest(): TelegramBroadcast? = latestId?.let(runs::get)

    /**
     * Validates the message, snapshots the recipient list and launches delivery in the background.
     * Returns immediately with the initial [TelegramBroadcast] state.
     */
    fun start(text: String, silent: Boolean): TelegramBroadcast {
        if (text.isBlank()) {
            throw ValidationException("Broadcast message must not be blank")
        }
        // Parse the markup once up front: a malformed message should fail the request outright
        // instead of failing separately for every single chat.
        try {
            TelegramHtmlMapper.fromHtml(text)
        } catch (e: Exception) {
            throw ValidationException("Failed to parse message HTML: ${e.message}")
        }
        synchronized(startLock) {
            runs.values.find { it.status == Status.RUNNING }?.let {
                throw ValidationException("Broadcast ${it.id} is already in progress")
            }
            val recipients = collectRecipients()
            if (recipients.isEmpty()) {
                throw ValidationException("There are no enabled chats to broadcast to")
            }
            val broadcast = TelegramBroadcast(
                id = UUID.randomUUID(),
                status = Status.RUNNING,
                silent = silent,
                total = recipients.size,
                sent = 0,
                failed = 0,
                failures = emptyList(),
                startedAt = ZonedDateTime.now(),
                finishedAt = null,
                error = null,
            )
            runs[broadcast.id] = broadcast
            latestId = broadcast.id
            pruneOldRuns()
            scope.launch { execute(broadcast.id, recipients, text, silent) }
            log.info("Started broadcast {} to {} chat(s)", broadcast.id, recipients.size)
            return broadcast
        }
    }

    private suspend fun execute(id: UUID, recipients: List<Recipient>, text: String, silent: Boolean) {
        try {
            for (recipient in recipients) {
                deliver(id, recipient, text, silent)
            }
            update(id) { it.copy(status = Status.COMPLETED, finishedAt = ZonedDateTime.now()) }
            findById(id)?.let { log.info("Broadcast {} finished: {} sent, {} failed", id, it.sent, it.failed) }
        } catch (e: CancellationException) {
            update(id) { it.copy(status = Status.FAILED, finishedAt = ZonedDateTime.now(), error = "Cancelled") }
            throw e
        } catch (e: Throwable) {
            log.error("Broadcast {} aborted", id, e)
            update(id) {
                it.copy(status = Status.FAILED, finishedAt = ZonedDateTime.now(), error = describe(e))
            }
        }
    }

    private suspend fun deliver(id: UUID, recipient: Recipient, text: String, silent: Boolean) {
        try {
            withTimeout(DELIVERY_TIMEOUT) {
                telegram.sendMessage(recipient.chatId, text, disableNotification = silent)
            }
            update(id) { it.copy(sent = it.sent + 1) }
        } catch (e: TimeoutCancellationException) {
            // Only the delivery scope was cancelled, the run itself carries on with the next chat.
            recordFailure(id, recipient, "Timed out after $DELIVERY_TIMEOUT", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            recordFailure(id, recipient, describe(e), e)
        }
    }

    private fun recordFailure(id: UUID, recipient: Recipient, reason: String, cause: Throwable) {
        log.warn("Broadcast {}: delivery to chat {} failed: {}", id, recipient.chatId, reason, cause)
        val failure = TelegramBroadcast.Failure(recipient.chatId, recipient.title, reason)
        update(id) {
            it.copy(
                failed = it.failed + 1,
                failures = if (it.failures.size < MAX_STORED_FAILURES) it.failures + failure else it.failures,
            )
        }
    }

    private fun collectRecipients(): List<Recipient> {
        val recipients = mutableListOf<Recipient>()
        var pageIndex = 0
        while (true) {
            val page = chatOrm.findAllEnabled(PageRequest.of(pageIndex, CHATS_PAGE_SIZE, Sort.by("id")))
            page.content.forEach { recipients.add(Recipient(it.id, titleOf(it))) }
            if (!page.hasNext()) {
                return recipients
            }
            pageIndex++
        }
    }

    private fun titleOf(chat: TelegramChat): String? {
        return chat.title
            ?: listOfNotNull(chat.firstName, chat.lastName).joinToString(" ").takeIf { it.isNotBlank() }
            ?: chat.username?.let { "@$it" }
    }

    private fun describe(e: Throwable): String = e.message ?: e::class.simpleName ?: "Unknown error"

    private fun update(id: UUID, block: (TelegramBroadcast) -> TelegramBroadcast) {
        runs.compute(id) { _, current -> current?.let(block) }
    }

    private fun pruneOldRuns() {
        val removable = runs.values
            .filter { it.status != Status.RUNNING && it.id != latestId }
            .sortedBy { it.startedAt }
        val excess = runs.size - MAX_STORED_RUNS
        if (excess <= 0) {
            return
        }
        removable.take(excess).forEach { runs.remove(it.id) }
    }

    private data class Recipient(val chatId: Long, val title: String?)
}
