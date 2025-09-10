package space.davids_digital.kiri.orm.service.telegram

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatEntity
import space.davids_digital.kiri.orm.mapper.TelegramChatMetadataEntityMapper
import space.davids_digital.kiri.orm.mapper.telegram.TelegramChatEntityMapper
import space.davids_digital.kiri.orm.repository.TelegramChatMetadataRepository
import space.davids_digital.kiri.orm.repository.telegram.TelegramChatRepository
import space.davids_digital.kiri.orm.specifications.telegram.TelegramChatSpecifications.titleContains
import space.davids_digital.kiri.orm.specifications.telegram.TelegramChatSpecifications.typeIn
import space.davids_digital.kiri.orm.specifications.telegram.TelegramChatSpecifications.usernameContains
import space.davids_digital.kiri.service.TelegramChatMetadataService
import space.davids_digital.kiri.service.exception.ServiceException

@Service
class TelegramChatOrmService(
    private val repo: TelegramChatRepository,
    private val metadataRepo: TelegramChatMetadataRepository,
    private val mapper: TelegramChatEntityMapper,
    private val metadataMapper: TelegramChatMetadataEntityMapper,
    private val telegramChatMetadataService: TelegramChatMetadataService
) {
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<TelegramChat> {
        return repo.findAll(pageable).map(::toModel)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): TelegramChat? {
        return repo.findById(id).map(::toModel).orElse(null)
    }

    @Transactional(readOnly = true)
    fun findByIds(ids: Collection<Long>): List<TelegramChat> {
        return repo.findAllById(ids).mapNotNull(::toModel)
    }

    @Transactional(readOnly = true)
    fun findByUsername(username: String): TelegramChat? {
        return repo.findByUsername(username)?.let(::toModel)
    }

    @Transactional(readOnly = true)
    fun search(
        query: String?,
        title: String?,
        username: String?,
        typeIn: List<TelegramChat.Type>,
        pageable: Pageable
    ): Page<TelegramChat> {
        var spec: Specification<TelegramChatEntity>? = null

        query?.takeIf { it.isNotBlank() }?.let {
            spec = titleContains(it).or(usernameContains(it))
        }
        title?.takeIf { it.isNotBlank() }?.let {
            val s = titleContains(it)
            spec = spec?.and(s) ?: s
        }
        username?.takeIf { it.isNotBlank() }?.let {
            val s = usernameContains(it)
            spec = spec?.and(s) ?: s
        }
        if (typeIn.isNotEmpty()) {
            val s = typeIn(typeIn.mapNotNull(mapper::toEntity))
            spec = spec?.and(s) ?: s
        }

        return repo.findAll(spec, pageable).map(::toModel)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return repo.existsById(id)
    }

    @Transactional
    fun save(chat: TelegramChat): TelegramChat {
        val chatEntity = mapper.toEntity(chat)!!
        val metadataEntity = metadataMapper.toEntity(chat.metadata, chat.id)!!
        val savedChatEntity = repo.save(chatEntity)
        val savedMetadata = metadataMapper.toModel(metadataRepo.save(metadataEntity))!!
        return toModel(savedChatEntity, savedMetadata)
    }

    @Transactional
    fun update(id: Long, block: TelegramChat.() -> TelegramChat): TelegramChat {
        var model = repo.findById(id).map(::toModel).orElseThrow { ServiceException("chat id $id not found") }!!
        model = model.block()
        return save(model)
    }

    @Transactional
    fun delete(id: Long) {
        repo.deleteById(id)
    }

    private fun toModel(chatEntity: TelegramChatEntity, metadata: TelegramChat.Metadata): TelegramChat {
        return mapper.toModel(chatEntity, metadata)!!
    }

    private fun toModel(chatEntity: TelegramChatEntity): TelegramChat {
        val metadata = telegramChatMetadataService.getOrCreateDefault(chatEntity.id, mapper.toModel(chatEntity.type)!!)
        return mapper.toModel(chatEntity, metadata)!!
    }
}