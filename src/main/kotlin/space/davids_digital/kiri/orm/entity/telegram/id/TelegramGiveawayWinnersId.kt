package space.davids_digital.kiri.orm.entity.telegram.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class TelegramGiveawayWinnersId(
    @Column(name = "chat_id")
    var chatId: Long = 0,

    @Column(name = "giveaway_message_id")
    var giveawayMessageId: Long = 0
)