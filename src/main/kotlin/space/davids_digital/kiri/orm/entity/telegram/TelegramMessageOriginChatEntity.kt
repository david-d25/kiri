package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("chat")
class TelegramMessageOriginChatEntity : TelegramMessageOriginEntity() {
    @Column(name = "sender_chat_id")
    var senderChatId: Long = 0

    @Column(name = "author_signature")
    var authorSignature: String? = null
}