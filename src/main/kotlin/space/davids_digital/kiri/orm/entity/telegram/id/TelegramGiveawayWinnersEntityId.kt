package space.davids_digital.kiri.orm.entity.telegram.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class TelegramGiveawayWinnersEntityId(
    @Column(name = "chat_id")
    var chatId: Long = 0,

    @Column(name = "giveaway_message_id")
    var giveawayMessageId: Int = 0
) : Serializable