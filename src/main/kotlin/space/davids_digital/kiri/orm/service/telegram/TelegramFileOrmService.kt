package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramFile
import space.davids_digital.kiri.orm.mapping.telegram.TelegramFileEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramFileRepository

@Service
class TelegramFileOrmService(
    private val repo: TelegramFileRepository,
    private val mapper: TelegramFileEntityMapper,
) {
    @Transactional
    fun save(model: TelegramFile): TelegramFile {
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}