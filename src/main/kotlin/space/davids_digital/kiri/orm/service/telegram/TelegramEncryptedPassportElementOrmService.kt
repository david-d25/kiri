package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramEncryptedPassportElement
import space.davids_digital.kiri.orm.mapping.telegram.TelegramEncryptedPassportElementEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramEncryptedPassportElementRepository

@Service
class TelegramEncryptedPassportElementOrmService(
    private val repo: TelegramEncryptedPassportElementRepository,
    private val mapper: TelegramEncryptedPassportElementEntityMapper,
    private val passportFileOrm: TelegramPassportFileOrmService,
) {
    @Transactional
    fun save(model: TelegramEncryptedPassportElement): TelegramEncryptedPassportElement {
        model.frontSide?.let { passportFileOrm.save(it) }
        model.reverseSide?.let { passportFileOrm.save(it) }
        model.selfie?.let { passportFileOrm.save(it) }
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}