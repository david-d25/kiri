package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramPassportData
import space.davids_digital.kiri.orm.mapper.telegram.TelegramPassportDataEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramPassportDataRepository

@Service
class TelegramPassportDataOrmService(
    private val repo: TelegramPassportDataRepository,
    private val mapper: TelegramPassportDataEntityMapper,
    private val encryptedPassportElementOrm: TelegramEncryptedPassportElementOrmService
) {
    @Transactional
    fun save(model: TelegramPassportData): TelegramPassportData {
        model.data.forEach { encryptedPassportElementOrm.save(it) }
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}