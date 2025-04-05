package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramBackgroundType
import space.davids_digital.kiri.orm.mapping.telegram.TelegramBackgroundTypeEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramBackgroundTypeRepository

@Service
class TelegramBackgroundTypeOrmService(
    private val repo: TelegramBackgroundTypeRepository,
    private val mapper: TelegramBackgroundTypeEntityMapper,
    private val documentOrmService: TelegramDocumentOrmService,
) {
    @Transactional
    fun save(model: TelegramBackgroundType): TelegramBackgroundType {
        if (model is TelegramBackgroundType.Pattern) {
            documentOrmService.save(model.document)
        }
        if (model is TelegramBackgroundType.Wallpaper) {
            documentOrmService.save(model.document)
        }
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}