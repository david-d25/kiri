package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramPassportFile
import space.davids_digital.kiri.orm.mapping.telegram.TelegramPassportFileEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramPassportFileRepository

@Service
class TelegramPassportFileOrmService(
    private val repo: TelegramPassportFileRepository,
    private val mapper: TelegramPassportFileEntityMapper,
) {
    @Transactional
    fun save(model: TelegramPassportFile): TelegramPassportFile {
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}