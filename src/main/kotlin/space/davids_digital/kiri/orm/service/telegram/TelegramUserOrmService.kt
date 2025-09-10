package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.entity.telegram.TelegramUserEntity
import space.davids_digital.kiri.orm.mapper.TelegramUserMetadataEntityMapper
import space.davids_digital.kiri.orm.mapper.telegram.TelegramUserEntityMapper
import space.davids_digital.kiri.orm.repository.TelegramUserMetadataRepository
import space.davids_digital.kiri.orm.repository.telegram.TelegramUserRepository
import space.davids_digital.kiri.service.TelegramUserMetadataService

@Service
class TelegramUserOrmService(
    private val repo: TelegramUserRepository,
    private val mapper: TelegramUserEntityMapper,
    private val metadataRepo: TelegramUserMetadataRepository,
    private val metadataMapper: TelegramUserMetadataEntityMapper,
    private val telegramUserMetadataService: TelegramUserMetadataService
) {
    @Transactional
    fun save(telegramUser: TelegramUser): TelegramUser {
        val entity = mapper.toEntity(telegramUser)!!
        val metadataEntity = metadataMapper.toEntity(telegramUser.metadata, telegramUser.id)!!
        val savedEntity = repo.save(entity)
        val savedMetadata = metadataRepo.save(metadataEntity)
        return toModel(savedEntity, metadataMapper.toModel(savedMetadata)!!)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): TelegramUser? {
        return repo.findById(id).map(::toModel).orElse(null)
    }

    @Transactional(readOnly = true)
    fun findAllById(ids: Collection<Long>): List<TelegramUser> {
        return repo.findAllById(ids).map(::toModel)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return repo.existsById(id)
    }

    private fun toModel(entity: TelegramUserEntity, metadata: TelegramUser.Metadata): TelegramUser {
        return mapper.toModel(entity, metadata)!!
    }

    private fun toModel(entity: TelegramUserEntity): TelegramUser {
        val metadata = telegramUserMetadataService.getOrCreateDefault(entity.id)
        return mapper.toModel(entity, metadata)!!
    }
}