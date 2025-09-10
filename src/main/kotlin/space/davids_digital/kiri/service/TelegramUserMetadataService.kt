package space.davids_digital.kiri.service

import org.springframework.stereotype.Service
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.service.TelegramUserMetadataOrmService

@Service
class TelegramUserMetadataService (private val telegramUserMetadataOrmService: TelegramUserMetadataOrmService) {
    fun getOrCreateDefault(userId: Long): TelegramUser.Metadata {
        return telegramUserMetadataOrmService.findByUserId(userId) ?: getDefault()
    }

    fun getDefault(): TelegramUser.Metadata {
        return TelegramUser.Metadata(
            isBlocked = false
        )
    }
}