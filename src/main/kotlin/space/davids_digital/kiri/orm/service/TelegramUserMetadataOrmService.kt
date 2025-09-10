package space.davids_digital.kiri.orm.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.mapper.TelegramUserMetadataEntityMapper
import space.davids_digital.kiri.orm.repository.TelegramUserMetadataRepository

@Service
class TelegramUserMetadataOrmService (
    private val repo: TelegramUserMetadataRepository,
    private val mapper: TelegramUserMetadataEntityMapper
) {
    @Transactional(readOnly = true)
    fun findByUserId(userId: Long): TelegramUser.Metadata? {
        return mapper.toModel(repo.findByIdOrNull(userId))
    }
}