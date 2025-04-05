package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramGiveawayWinners
import space.davids_digital.kiri.orm.mapping.telegram.TelegramGiveawayWinnersEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramGiveawayWinnersRepository

@Service
class TelegramGiveawayWinnersOrmService(
    private val repo: TelegramGiveawayWinnersRepository,
    private val mapper: TelegramGiveawayWinnersEntityMapper,
) {
    @Transactional
    fun save(message: TelegramGiveawayWinners): TelegramGiveawayWinners {
        return mapper.toModel(repo.save(mapper.toEntity(message)!!))!!
    }
}