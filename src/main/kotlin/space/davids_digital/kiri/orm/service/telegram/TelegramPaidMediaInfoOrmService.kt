package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramPaidMediaInfo
import space.davids_digital.kiri.orm.mapper.telegram.TelegramPaidMediaInfoEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramPaidMediaInfoRepository

@Service
class TelegramPaidMediaInfoOrmService(
    private val repo: TelegramPaidMediaInfoRepository,
    private val mapper: TelegramPaidMediaInfoEntityMapper,
    private val paidMediaOrmService: TelegramPaidMediaOrmService,
) {
    @Transactional
    fun save(model: TelegramPaidMediaInfo): TelegramPaidMediaInfo {
        model.paidMedia.forEach(paidMediaOrmService::save)
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}