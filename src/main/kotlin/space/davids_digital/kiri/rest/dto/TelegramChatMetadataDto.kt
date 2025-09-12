package space.davids_digital.kiri.rest.dto

import java.time.ZonedDateTime

data class TelegramChatMetadataDto(
    val lastReadMessageId: Int?,
    val notificationMode: NotificationMode,
    val mutedUntil: ZonedDateTime?,
    val archived: Boolean,
    val pinned: Boolean,
    val enabled: Boolean,
) {
    enum class NotificationMode {
        ALL, ONLY_MENTIONS, NONE
    }
}
