package space.davids_digital.kiri.orm.service.telegram

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramGiveawayCompleted
import space.davids_digital.kiri.orm.mapping.telegram.TelegramGiveawayCompletedEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramGiveawayCompletedRepository

@Service
class TelegramGiveawayCompletedOrmService(
    private val repo: TelegramGiveawayCompletedRepository,
    private val mapper: TelegramGiveawayCompletedEntityMapper,
) {
    @Lazy
    @Autowired
    private lateinit var messageOrm: TelegramMessageOrmService

    @Transactional
    fun save(model: TelegramGiveawayCompleted): TelegramGiveawayCompleted {
        model.message?.let(messageOrm::save)
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}