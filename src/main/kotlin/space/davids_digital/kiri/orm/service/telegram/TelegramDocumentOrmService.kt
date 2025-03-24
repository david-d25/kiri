package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramDocument
import space.davids_digital.kiri.orm.mapping.TelegramDocumentEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramDocumentRepository

@Service
class TelegramDocumentOrmService(
    private val repo: TelegramDocumentRepository,
    private val mapper: TelegramDocumentEntityMapper,
    private val photoSizeOrmService: TelegramPhotoSizeOrmService
) {
    @Transactional
    fun save(document: TelegramDocument): TelegramDocument {
        document.thumbnail?.let { photoSizeOrmService.save(it) }
        return mapper.toModel(repo.save(mapper.toEntity(document)!!))!!
    }
}