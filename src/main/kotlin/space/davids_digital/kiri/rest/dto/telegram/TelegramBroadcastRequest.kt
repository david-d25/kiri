package space.davids_digital.kiri.rest.dto.telegram

data class TelegramBroadcastRequest(
    val text: String,
    val silent: Boolean = false,
)
