package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramMessageEntity
import space.davids_digital.kiri.orm.mapping.TelegramMessageEntityEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramMessageEntityRepository

@Service
class TelegramMessageEntityOrmService(
    private val repo: TelegramMessageEntityRepository,
    private val mapper: TelegramMessageEntityEntityMapper
) {
    @Transactional
    fun save(model: TelegramMessageEntity): TelegramMessageEntity {
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}