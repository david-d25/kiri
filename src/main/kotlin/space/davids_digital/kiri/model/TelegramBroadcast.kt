package space.davids_digital.kiri.model

import java.time.ZonedDateTime
import java.util.UUID

/**
 * State of a single broadcast run: one message delivered to every enabled Telegram chat.
 *
 * Delivery failures for individual chats (bot blocked, kicked from group, chat deleted) never abort the run,
 * they are collected into [failures] instead.
 */
data class TelegramBroadcast(
    val id: UUID,
    val status: Status,
    val silent: Boolean,
    val total: Int,
    val sent: Int,
    val failed: Int,
    val failures: List<Failure>,
    val startedAt: ZonedDateTime,
    val finishedAt: ZonedDateTime?,
    /** Set only when the run itself broke down, as opposed to individual chats failing. */
    val error: String?,
) {
    val processed: Int get() = sent + failed

    data class Failure(
        val chatId: Long,
        val chatTitle: String?,
        val error: String,
    )

    enum class Status {
        RUNNING, COMPLETED, FAILED
    }
}
