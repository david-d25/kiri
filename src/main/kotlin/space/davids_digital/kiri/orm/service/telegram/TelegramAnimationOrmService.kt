package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramAnimation
import space.davids_digital.kiri.orm.mapping.telegram.TelegramAnimationEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramAnimationRepository

@Service
class TelegramAnimationOrmService(
    private val repo: TelegramAnimationRepository,
    private val mapper: TelegramAnimationEntityMapper,
    private val photoSizeOrm: TelegramPhotoSizeOrmService
) {
    @Transactional
    fun save(model: TelegramAnimation): TelegramAnimation {
        model.thumbnail?.let { photoSizeOrm.save(it) }
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}