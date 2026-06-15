package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageEntityId
import java.time.OffsetDateTime

/**
 * Aggregate reactions of a message, stored independently of [TelegramMessageEntity] (see V11 migration).
 * [reactions] is a JSON array serialized by the application layer.
 */
@Entity
@Table(schema = "telegram", name = "message_reactions")
class TelegramMessageReactionsEntity {
    @EmbeddedId
    var id: TelegramMessageEntityId = TelegramMessageEntityId()

    @Column(name = "reactions")
    var reactions: String = "[]"

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
}
