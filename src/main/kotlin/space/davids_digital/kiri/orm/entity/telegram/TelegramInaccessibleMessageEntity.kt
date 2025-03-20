package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageId

@Entity
@Table(name = "telegram_inaccessible_messages")
class TelegramInaccessibleMessageEntity {
    @EmbeddedId
    var id: TelegramMessageId = TelegramMessageId()
}
