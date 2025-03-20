package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("hidden_user")
class TelegramMessageOriginHiddenUserEntity : TelegramMessageOriginEntity() {
    @Column(name = "sender_user_name")
    var senderUserName: String = ""
}