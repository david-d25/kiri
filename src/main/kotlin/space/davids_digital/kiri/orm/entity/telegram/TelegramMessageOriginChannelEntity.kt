package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("channel")
class TelegramMessageOriginChannelEntity : TelegramMessageOriginEntity() {
    @Column(name = "chat_id")
    var chatId: Long = 0

    @Column(name = "message_id")
    var messageId: Long = 0

    @Column(name = "author_signature")
    var authorSignature: String? = null
}