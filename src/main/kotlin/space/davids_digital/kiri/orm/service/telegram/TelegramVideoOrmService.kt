package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramVideo
import space.davids_digital.kiri.orm.mapping.TelegramVideoEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramVideoRepository

@Service
class TelegramVideoOrmService(
    private val repo: TelegramVideoRepository,
    private val mapper: TelegramVideoEntityMapper,
    private val photoSizeOrm: TelegramPhotoSizeOrmService,
) {
    @Transactional
    fun save(video: TelegramVideo): TelegramVideo {
        video.thumbnail?.let { photoSizeOrm.save(it) }
        return mapper.toModel(repo.save(mapper.toEntity(video)!!))!!
    }
}