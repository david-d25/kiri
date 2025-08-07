package space.davids_digital.kiri.rest.dto.telegram

data class TelegramChatFetchResultDto (
    val found: Boolean,
    val chat: TelegramChatDto? = null,
)