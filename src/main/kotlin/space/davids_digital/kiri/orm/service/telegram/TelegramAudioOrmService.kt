package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramAudio
import space.davids_digital.kiri.orm.mapper.telegram.TelegramAudioEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramAudioRepository

@Service
class TelegramAudioOrmService(
    private val repo: TelegramAudioRepository,
    private val mapper: TelegramAudioEntityMapper,
    private val photoSizeOrm: TelegramPhotoSizeOrmService,
) {
    @Transactional
    fun save(audio: TelegramAudio): TelegramAudio {
        audio.thumbnail?.let { photoSizeOrm.save(it) }
        return mapper.toModel(repo.save(mapper.toEntity(audio)!!))!!
    }
}