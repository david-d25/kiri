package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramPollOption
import space.davids_digital.kiri.orm.mapping.TelegramPollOptionEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramPollOptionRepository

@Service
class TelegramPollOptionOrmService(
    private val repo: TelegramPollOptionRepository,
    private val mapper: TelegramPollOptionEntityMapper,
    private val entityOrmService: TelegramMessageEntityOrmService,
) {
    @Transactional
    fun save(model: TelegramPollOption): TelegramPollOption {
        model.textEntities.forEach(entityOrmService::save)
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}