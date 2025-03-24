package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramPoll
import space.davids_digital.kiri.orm.mapping.TelegramPollEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramPollRepository

@Service
class TelegramPollOrmService(
    private val repo: TelegramPollRepository,
    private val mapper: TelegramPollEntityMapper,
) {
    @Transactional
    fun save(model: TelegramPoll): TelegramPoll {
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}