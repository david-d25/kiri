package space.davids_digital.kiri.rest.dto.telegram

data class TelegramBroadcastStatusDto(
    /** How many enabled chats a broadcast started right now would be delivered to. */
    val recipients: Long,
    /** Currently running broadcast, or the most recent finished one, if any. */
    val latest: TelegramBroadcastDto?,
)
