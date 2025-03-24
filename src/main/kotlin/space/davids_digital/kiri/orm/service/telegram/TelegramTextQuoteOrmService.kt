package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramTextQuote
import space.davids_digital.kiri.orm.mapping.TelegramTextQuoteEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramTextQuoteRepository

@Service
class TelegramTextQuoteOrmService(
    private val repo: TelegramTextQuoteRepository,
    private val mapper: TelegramTextQuoteEntityMapper,
    private val messageEntityOrmService: TelegramMessageEntityOrmService,
) {
    @Transactional
    fun save(model: TelegramTextQuote): TelegramTextQuote {
        model.entities.forEach(messageEntityOrmService::save)
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}