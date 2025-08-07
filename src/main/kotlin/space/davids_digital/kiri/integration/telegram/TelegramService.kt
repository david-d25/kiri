package space.davids_digital.kiri.integration.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramException
import com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.model.request.InputMediaPhoto
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.model.request.ReplyParameters
import com.pengrad.telegrambot.request.*
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.utility.kotlin.extension.request.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import space.davids_digital.kiri.AppProperties
import space.davids_digital.kiri.agent.engine.EngineEventBus
import space.davids_digital.kiri.agent.engine.WakeUpRequestEvent
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.notification.Notification
import space.davids_digital.kiri.agent.notification.NotificationManager
import space.davids_digital.kiri.model.telegram.*
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramMessageOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramUserOrmService
import space.davids_digital.kiri.service.exception.ServiceException
import java.time.ZonedDateTime
import kotlin.math.min

@Service
class TelegramService(
    private val messageOrm: TelegramMessageOrmService,
    private val chatOrm: TelegramChatOrmService,
    private val userOrm: TelegramUserOrmService,
    private val appProperties: AppProperties,
    private val engineEventBus: EngineEventBus,
    private val notificationManager: NotificationManager,
    @Lazy private val self: TelegramService
) {
    companion object {
        private const val MAX_MESSAGE_LENGTH = 4_096
        private const val MAX_CAPTION_LENGTH = 1_024
        private const val MAX_MEDIA_GROUP_SIZE = 10
        private const val MAX_MESSAGES_PER_SECOND_FREE = 30
        private const val MAX_MESSAGES_PER_SECOND_PAID = 1000
    }

    private val log = LoggerFactory.getLogger(this::class.java)
    private val openedChats = mutableSetOf<Long>()

    private lateinit var bot: TelegramBot

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val updatesInternal = MutableSharedFlow<TelegramUpdate>(replay = 0, extraBufferCapacity = 64)
    private val rateLimiter = RateLimiter(MAX_MESSAGES_PER_SECOND_FREE)

    val updates: SharedFlow<TelegramUpdate> = updatesInternal.asSharedFlow()

    private class RateLimiter(private val maxPerSecond: Int) {
        private var sentInWindow = 0
        private var windowStart = System.currentTimeMillis()
        suspend fun acquire() {
            val now = System.currentTimeMillis()
            if (now - windowStart >= 1_000) {
                windowStart = now
                sentInWindow = 0
            }
            if (sentInWindow >= maxPerSecond) {
                delay(1_000 - (now - windowStart))
                windowStart = System.currentTimeMillis()
                sentInWindow = 0
            }
            sentInWindow++
        }
    }

    @PostConstruct
    fun start() {
        bot = TelegramBot(appProperties.integration.telegram.apiKey)
        bot.setUpdatesListener(::processUpdates, ::onBotException)
        updateSelfInfo()
    }

    suspend fun sendText(chatId: Long, text: String, textEntities: List<TelegramMessageEntity> = emptyList()) {
        bot.sendMessage(chatId, text) {
            entities(*textEntities.map { it.toDto() }.toTypedArray())
        }.checkNoErrors("Failed to send message to chat id $chatId").message()
    }

    suspend fun forwardMessage(
        chatId: Long,
        fromChatId: Long,
        messageId: Int,
        disableNotification: Boolean = false
    ) {
        bot.forwardMessage(chatId, fromChatId, messageId) {
            disableNotification(disableNotification)
        }.checkNoErrors("Failed to forward message $messageId from chat $fromChatId to chat $chatId").message()
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        textEntities: List<TelegramMessageEntity> = emptyList(),
        images: List<ByteArray> = emptyList(),
        replyMarkup: TelegramInlineKeyboardMarkup? = null,
        disableNotification: Boolean = false,
        messageThreadId: Int? = null,
        replyToMessageId: Int? = null
    ) = sendMessage(
        listOf(chatId),
        text,
        textEntities,
        images,
        replyMarkup,
        disableNotification,
        messageThreadId,
        replyToMessageId
    )

    suspend fun sendMessage(
        chatIds: List<Long>,
        text: String,
        textEntities: List<TelegramMessageEntity> = emptyList(),
        images: List<ByteArray> = emptyList(),
        replyMarkup: TelegramInlineKeyboardMarkup? = null,
        disableNotification: Boolean = false,
        messageThreadId: Int? = null,
        replyToMessageId: Int? = null
    ) {
        for (chatId in chatIds) {
            val requests = buildSendMessageRequests(
                chatId,
                text,
                textEntities,
                images,
                replyMarkup,
                disableNotification,
                messageThreadId,
                replyToMessageId
            )

            for (request in requests) {
                var sent = false
                while (!sent) {
                    rateLimiter.acquire()
                    try {
                        val response = bot.execute(request)
                        if (response.isOk) {
                            sent = true
                        } else if (response.errorCode() == 429) {
                            val retryAfter = min(response.parameters().retryAfter(), 1)
                            log.warn("429 received, backing off for {} s", retryAfter)
                            delay(retryAfter * 1_000L)
                        } else {
                            log.warn(
                                "Failed to send message to {} ({}): {}",
                                chatId,
                                response.errorCode(),
                                response.description()
                            )
                            sent = true
                        }
                    } catch (ex: Exception) {
                        log.warn("Exception while sending message to chat {}", chatId, ex)
                    }
                }
            }
        }
    }

    private suspend fun buildSendMessageRequests(
        chatId: Long,
        text: String,
        textEntities: List<TelegramMessageEntity>,
        images: List<ByteArray>,
        replyMarkup: TelegramInlineKeyboardMarkup?,
        disableNotification: Boolean,
        messageThreadId: Int? = null,
        replyToMessageId: Int? = null
    ): List<BaseRequest<*, *>> {
        val firstLimit = if (images.isNotEmpty()) MAX_CAPTION_LENGTH else MAX_MESSAGE_LENGTH
        val slices     = splitTextVariableLimit(text, textEntities, firstLimit)

        return slices.mapIndexed { index, slice ->
            buildSendMessageRequest(
                chatId              = chatId,
                text                = slice.text,
                textEntities        = slice.entities,
                images              = if (index == 0) images else emptyList(),
                replyMarkup         = if (index == slices.lastIndex) replyMarkup else null,
                disableNotification = disableNotification,
                messageThreadId     = messageThreadId,
                replyToMessageId    = replyToMessageId
            )
        }
    }

    private suspend fun buildSendMessageRequest(
        chatId: Long,
        text: String,
        textEntities: List<TelegramMessageEntity>,
        images: List<ByteArray>,
        replyMarkup: TelegramInlineKeyboardMarkup?,
        disableNotification: Boolean,
        messageThreadId: Int? = null,
        replyToMessageId: Int? = null
    ): BaseRequest<*, *> {
        require(images.size <= 10) { "Telegram allows up to 10 images per message" }
        return when (images.size) {
            0 -> SendMessage(chatId, text).apply {
                if (textEntities.isNotEmpty()) entities(*textEntities.map { it.toDto() }.toTypedArray())
                replyMarkup?.let { replyMarkup(it.toDto()) }
                disableNotification(disableNotification)
                messageThreadId?.let { messageThreadId(it) }
                replyToMessageId?.let { replyParameters(ReplyParameters(it)) }
                parseMode(ParseMode.HTML)
            }

            1 -> SendPhoto(chatId, images.first()).apply {
                caption(text)
                if (textEntities.isNotEmpty()) captionEntities(*textEntities.map { it.toDto() }.toTypedArray())
                replyMarkup?.let { replyMarkup(it.toDto()) }
                disableNotification(disableNotification)
                messageThreadId?.let { messageThreadId(it) }
                replyToMessageId?.let { replyParameters(ReplyParameters(it)) }
                parseMode(ParseMode.HTML)
            }

            else -> {
                val media = images.mapIndexed { index, bytes -> InputMediaPhoto(bytes) }.toMutableList()
                media.first().caption(text)
                media.first().captionEntities(*textEntities.map { it.toDto() }.toTypedArray())
                media.first().parseMode(ParseMode.HTML)

                SendMediaGroup(chatId, *media.toTypedArray()).apply {
                    disableNotification(disableNotification)
                    messageThreadId?.let { messageThreadId(it) }
                    replyToMessageId?.let { replyParameters(ReplyParameters(it)) }
                }
            }
        }
    }

    suspend fun editMessage(
        chatId: Long,
        messageId: Int,
        text: String,
        replyMarkup: TelegramInlineKeyboardMarkup? = null
    ) {
        val response = bot.execute(EditMessageText(chatId, messageId, text).apply {
            replyMarkup?.let { replyMarkup(it.toDto()) }
        })
        if (response.errorCode() == 400 && response.description().contains("exactly the same")) {
            log.debug(
                "Message $messageId in chat $chatId was not edited because the text and reply markup are the same " +
                        "as before."
            )
            return
        }
        response.checkNoErrors("Failed to edit message $messageId in chat $chatId")
    }

    @Cacheable(value = ["TelegramService#fetchAndSaveChatById"], key = "#chatId")
    suspend fun fetchAndSaveChatById(chatId: Long): TelegramChat? {
        val response = bot.getChat(chatId)
        if (response.errorCode() == 400) {
            return null
        }
        response.checkNoErrors("Failed to get chat id $chatId")
        return chatOrm.save(response.chat().toModel())
    }

    @Cacheable(value = ["TelegramService#fetchAndSaveChatByUsername"], key = "#username")
    suspend fun fetchAndSaveChatByUsername(username: String): TelegramChat? {
        val response = bot.getChat("@$username")
        if (response.errorCode() == 400) {
            return null
        }
        response.checkNoErrors("Failed to get chat by username '$username'")
        return chatOrm.save(response.chat().toModel())
    }

    suspend fun answerCallbackQuery(callbackQueryId: String, text: String? = null) {
        bot.execute(AnswerCallbackQuery(callbackQueryId).apply {
            text?.let { text(it) }
        }).checkNoErrors("Failed to answer callback query")
    }

    fun declareChatOpened(chatId: Long) {
        openedChats.add(chatId)
    }

    fun declareChatClosed(chatId: Long) {
        openedChats.remove(chatId)
    }

    suspend fun getChats(): List<TelegramChat> {
        return chatOrm.getAllChats()
    }

    suspend fun getChat(username: String): TelegramChat? {
        val savedChat = chatOrm.findByUsername(username)
        if (savedChat != null) {
            return savedChat
        }
        return self.fetchAndSaveChatByUsername(username)
    }

    suspend fun getChat(id: Long): TelegramChat? {
        val savedChat = chatOrm.findById(id)
        if (savedChat != null) {
            return savedChat
        }
        return self.fetchAndSaveChatById(id)
    }

    suspend fun chatExists(id: Long): Boolean {
        if (chatOrm.existsById(id)) {
            return true
        }
        try {
            bot.getChat(id).checkNoErrors().chat().toModel().let { chatOrm.save(it) }
            return true
        } catch (e: Exception) {
            log.info("Could not fetch chat with id $id (${e.message}), will assume it doesn't exist")
            return false
        }
    }

    fun getUser(id: Long) = userOrm.findById(id)

    suspend fun getChatMessages(chatId: Long, limit: Long): List<TelegramMessage> {
        return messageOrm.getChatMessages(chatId, limit)
    }

    @Cacheable(value = ["TelegramService#getFileContent"], key = "#fileId")
    suspend fun getFileContent(fileId: String): ByteArray {
        return bot.getFileContent(bot.getFile(fileId).file())
    }

    private suspend fun onMessageReceived(message: TelegramMessage) {
        log.debug("Received Telegram message from chat {}", message.chatId)
        try {
            messageOrm.save(message)
        } catch (e: Exception) {
            log.error("Failed to save Telegram message", e)
        }
        if (needToSendNotification(message)) {
            sendNewMessageNotification(message)
        } else if (!openedChats.contains(message.chatId)) {
            engineEventBus.tryPublish(WakeUpRequestEvent())
        }
    }

    private suspend fun needToSendNotification(message: TelegramMessage): Boolean {
        val chat = getChat(message.chatId)
        if (chat == null) {
            log.warn("Received message from unknown chat with id {}", message.chatId)
            return false
        }
        return chat.type == TelegramChat.Type.PRIVATE
    }

    private suspend fun sendNewMessageNotification(message: TelegramMessage) {
        val fromId = message.fromId
        if (fromId == null) {
            log.error("Received message without sender ID, cannot send notification")
            return
        }
        val user = getUser(fromId)
        if (user == null) {
            log.error("Received message from unknown user with ID {}", fromId)
        }
        notificationManager.push(Notification(
            sentAt = ZonedDateTime.now(),
            metadata = mapOf(
                "app" to "telegram",
                "chat-id" to message.chatId.toString()
            ),
            content = dataFrameContent {
                val from = user?.firstName ?: "(user)"
                if (message.text.isNullOrBlank()) {
                    text("New message from $from")
                } else {
                    var text = message.text.take(128)
                    if (text.length > 128) {
                        text = text.substring(0, 128) + "..."
                    }
                    text("New message from $from: $text")
                }
            }
        ))
    }

    private fun refreshUser(user: User) {
        userOrm.save(user.toModel())
    }

    private fun refreshChat(chat: Chat) {
        val id = chat.id()
        try {
            if (!chatOrm.existsById(id)) {
                log.debug("Chat with id $id does not exist, fetching and saving")
                val chat = bot.getChat(id)
                    .checkNoErrors("Failed to get Telegram chat with id $id")
                    .chat()
                    .toModel()
                chatOrm.save(chat)
            }
        } catch (e: Exception) {
            log.error("Failed to get and save Telegram chat", e)
        }
    }

    private fun updateSelfInfo() {
        try {
            userOrm.save(bot.getMe().user().toModel())
        } catch (e: Exception) {
            log.error("Failed to get and save bot info", e)
        }
    }

    private fun processUpdates(updates: List<Update>): Int {
        serviceScope.launch {
            updates.forEach { update ->
                val updateModel = try {
                    update.toModel()
                } catch (e: Exception) {
                    log.error("Failed to create update model, it will be skipped", e)
                    return@forEach
                }
                if (updateModel.message != null) {
                    onMessageReceived(updateModel.message)
                }
                updatesInternal.emit(updateModel)
                // todo refresh users from here
            }
        }
        return CONFIRMED_UPDATES_ALL
    }

    private fun onBotException(e: TelegramException) {
        if (e.response() != null) {
            log.error("Telegram API error ({}): {}", e.response().errorCode(), e.response().description(), e)
        } else {
            log.error("Telegram API error", e)
        }
    }

    private fun <T : BaseResponse> T.checkNoErrors(customMessage: String? = null): T {
        if (isOk) {
            return this
        }
        log.error("Telegram API error ({}): {}", errorCode(), description())
        if (customMessage != null) {
            throw ServiceException(customMessage + ": " + description())
        } else {
            throw ServiceException("Telegram API error (${errorCode()}): ${description()}")
        }
    }

    fun splitTextVariableLimit(
        fullText: String,
        entities: List<TelegramMessageEntity>,
        firstLimit: Int,
        otherLimit: Int = MAX_MESSAGE_LENGTH,  // 4096
    ): List<EntitySlice> {

        require(firstLimit in 1..otherLimit)
        require(entities.none { it.length > otherLimit }) {
            "Entity length exceeds Telegram hard limit ($otherLimit)"
        }

        val out = mutableListOf<EntitySlice>()
        var chunkStart = 0
        var isFirst = true

        while (chunkStart < fullText.length) {
            val limit = if (isFirst) firstLimit else otherLimit
            isFirst = false

            val hardEnd  = (chunkStart + limit).coerceAtMost(fullText.length)
            var chunkEnd = hardEnd

            val hitLimit = hardEnd == chunkStart + limit && hardEnd < fullText.length
            if (hitLimit) {
                entities.forEach { e ->
                    val eStart = e.offset
                    val eEnd   = e.offset + e.length
                    if (eStart < chunkEnd && eEnd > chunkEnd) chunkEnd = eStart
                }

                var optEnd = chunkEnd
                while (optEnd > chunkStart && !fullText[optEnd - 1].isWhitespace()) optEnd--
                if (optEnd - chunkStart >= 5) chunkEnd = optEnd
            }

            val partEntities = entities
                .filter { it.offset < chunkEnd && it.offset + it.length > chunkStart }
                .map { e ->
                    val start = (e.offset - chunkStart).coerceAtLeast(0)
                    val end   = (e.offset + e.length).coerceAtMost(chunkEnd) - chunkStart
                    e.copy(offset = start, length = end - start)
                }

            out += EntitySlice(fullText.substring(chunkStart, chunkEnd), partEntities)
            chunkStart = chunkEnd
        }
        return out
    }

    data class EntitySlice(
        val text: String,
        val entities: List<TelegramMessageEntity>
    )
}