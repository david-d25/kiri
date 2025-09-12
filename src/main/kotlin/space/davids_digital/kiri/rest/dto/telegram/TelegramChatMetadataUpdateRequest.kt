package space.davids_digital.kiri.rest.dto.telegram

import space.davids_digital.kiri.rest.dto.TelegramChatMetadataDto

data class TelegramChatMetadataUpdateRequest (
    val notificationMode: TelegramChatMetadataDto.NotificationMode?,
    val enabled: Boolean?
)