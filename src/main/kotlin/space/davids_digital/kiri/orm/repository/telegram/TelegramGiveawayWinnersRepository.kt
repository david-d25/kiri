package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.telegram.TelegramGiveawayWinnersEntity
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramGiveawayWinnersId

@Repository
interface TelegramGiveawayWinnersRepository: JpaRepository<TelegramGiveawayWinnersEntity, TelegramGiveawayWinnersId>