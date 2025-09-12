package space.davids_digital.kiri.service

import org.springframework.stereotype.Service
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.service.TelegramChatMetadataOrmService

@Service
class TelegramChatMetadataService (private val telegramChatMetadataOrmService: TelegramChatMetadataOrmService) {

    fun getOrCreateDefault(chatId: Long, type: TelegramChat.Type): TelegramChat.Metadata {
        return telegramChatMetadataOrmService.findByChatId(chatId) ?: getDefault(type)
    }

    fun getDefault(chatType: TelegramChat.Type): TelegramChat.Metadata {
        val notificationMode: TelegramChat.NotificationMode =
            if (chatType == TelegramChat.Type.PRIVATE || chatType == TelegramChat.Type.CHANNEL) {
                TelegramChat.NotificationMode.ALL
            } else {
                TelegramChat.NotificationMode.ONLY_MENTIONS
            }
        return TelegramChat.Metadata(
            lastReadMessageId = null,
            notificationMode = notificationMode,
            mutedUntil = null,
            archived = false,
            pinned = false,
            enabled = false
        )
    }
}