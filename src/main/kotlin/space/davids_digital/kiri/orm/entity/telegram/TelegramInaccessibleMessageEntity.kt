package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageEntityId

@Entity
@Table(schema = "telegram", name = "inaccessible_messages")
class TelegramInaccessibleMessageEntity {
    @EmbeddedId
    var id: TelegramMessageEntityId = TelegramMessageEntityId()
}
