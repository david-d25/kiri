package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramVideoNote
import space.davids_digital.kiri.orm.mapper.telegram.TelegramVideoNoteEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramVideoNoteRepository

@Service
class TelegramVideoNoteOrmService(
    private val repo: TelegramVideoNoteRepository,
    private val mapper: TelegramVideoNoteEntityMapper,
    private val photoSizeOrm: TelegramPhotoSizeOrmService,
) {
    @Transactional
    fun save(model: TelegramVideoNote): TelegramVideoNote {
        model.thumbnail?.let { photoSizeOrm.save(it) }
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}