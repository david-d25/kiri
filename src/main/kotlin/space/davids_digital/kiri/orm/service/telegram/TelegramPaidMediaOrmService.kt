package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramPaidMedia
import space.davids_digital.kiri.orm.mapper.telegram.TelegramPaidMediaEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramPaidMediaRepository

@Service
class TelegramPaidMediaOrmService(
    private val repo: TelegramPaidMediaRepository,
    private val mapper: TelegramPaidMediaEntityMapper,
    private val telegramVideoOrmService: TelegramVideoOrmService
) {
    @Transactional
    fun save(model: TelegramPaidMedia): TelegramPaidMedia {
        if (model is TelegramPaidMedia.Video) {
            telegramVideoOrmService.save(model.video)
        }
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}