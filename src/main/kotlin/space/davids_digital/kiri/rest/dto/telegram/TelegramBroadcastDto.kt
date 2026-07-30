package space.davids_digital.kiri.rest.dto.telegram

import java.time.ZonedDateTime
import java.util.UUID

data class TelegramBroadcastDto(
    val id: UUID,
    val status: Status,
    val silent: Boolean,
    val total: Int,
    val sent: Int,
    val failed: Int,
    val failures: List<Failure>,
    val startedAt: ZonedDateTime,
    val finishedAt: ZonedDateTime?,
    val error: String?,
) {
    data class Failure(
        val chatId: Long,
        val chatTitle: String?,
        val error: String,
    )

    enum class Status {
        RUNNING, COMPLETED, FAILED
    }
}
