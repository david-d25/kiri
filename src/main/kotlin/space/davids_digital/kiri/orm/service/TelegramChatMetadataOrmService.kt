package space.davids_digital.kiri.orm.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.mapper.TelegramChatMetadataEntityMapper
import space.davids_digital.kiri.orm.repository.TelegramChatMetadataRepository

@Service
class TelegramChatMetadataOrmService (
    private val repo: TelegramChatMetadataRepository,
    private val mapper: TelegramChatMetadataEntityMapper
) {
    @Transactional(readOnly = true)
    fun findByChatId(chatId: Long): TelegramChat.Metadata? {
        return mapper.toModel(repo.findByIdOrNull(chatId))
    }
}