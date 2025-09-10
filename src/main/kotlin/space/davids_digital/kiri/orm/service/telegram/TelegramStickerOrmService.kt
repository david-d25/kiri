package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramSticker
import space.davids_digital.kiri.orm.mapper.telegram.TelegramStickerEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramStickerRepository

@Service
class TelegramStickerOrmService(
    private val repo: TelegramStickerRepository,
    private val mapper: TelegramStickerEntityMapper,
    private val photoSizeOrm: TelegramPhotoSizeOrmService,
    private val fileOrm: TelegramFileOrmService,
) {
    @Transactional
    fun save(model: TelegramSticker): TelegramSticker {
        model.thumbnail?.let { photoSizeOrm.save(it) }
        model.premiumAnimation?.let { fileOrm.save(it) }
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}