package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramVoice
import space.davids_digital.kiri.orm.mapping.telegram.TelegramVoiceEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramVoiceRepository

@Service
class TelegramVoiceOrmService(
    private val repo: TelegramVoiceRepository,
    private val mapper: TelegramVoiceEntityMapper
) {
    @Transactional
    fun save(voice: TelegramVoice): TelegramVoice {
        return mapper.toModel(repo.save(mapper.toEntity(voice)!!))!!
    }
}