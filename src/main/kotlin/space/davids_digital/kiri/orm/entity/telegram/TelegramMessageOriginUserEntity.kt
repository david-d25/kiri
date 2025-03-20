package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("user")
class TelegramMessageOriginUserEntity : TelegramMessageOriginEntity() {
    @Column(name = "sender_user_id")
    var senderUserId: Long = 0
}