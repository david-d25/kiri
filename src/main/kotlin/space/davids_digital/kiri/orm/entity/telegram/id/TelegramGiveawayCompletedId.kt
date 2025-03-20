package space.davids_digital.kiri.orm.entity.telegram.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class TelegramGiveawayCompletedId(
    @Column(name = "chat_id")
    var chatId: Long = 0,

    @Column(name = "message_id")
    var messageId: Long = 0
)
