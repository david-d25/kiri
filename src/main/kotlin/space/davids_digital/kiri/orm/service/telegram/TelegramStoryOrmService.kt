package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramStory
import space.davids_digital.kiri.orm.mapper.telegram.TelegramStoryEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramStoryRepository

@Service
class TelegramStoryOrmService(
    private val repo: TelegramStoryRepository,
    private val mapper: TelegramStoryEntityMapper
) {
    @Transactional
    fun save(model: TelegramStory): TelegramStory {
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}