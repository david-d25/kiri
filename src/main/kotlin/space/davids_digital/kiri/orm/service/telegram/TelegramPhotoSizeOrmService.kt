package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramPhotoSize
import space.davids_digital.kiri.orm.mapping.TelegramPhotoSizeEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramPhotoSizeRepository

@Service
class TelegramPhotoSizeOrmService(
    private val repo: TelegramPhotoSizeRepository,
    private val mapper: TelegramPhotoSizeEntityMapper,
) {
    @Transactional
    fun save(photoSize: TelegramPhotoSize): TelegramPhotoSize {
        return mapper.toModel(repo.save(mapper.toEntity(photoSize)!!))!!
    }
}