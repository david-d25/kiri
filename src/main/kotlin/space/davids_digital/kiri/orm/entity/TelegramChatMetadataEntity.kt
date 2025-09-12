package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(schema = "main", name = "telegram_chat_metadata")
class TelegramChatMetadataEntity {
    @Id
    @Column(name = "chat_id")
    var chatId: Long = 0

    @Column(name = "last_read_message_id")
    var lastReadMessageId: Int? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_mode")
    var notificationMode: NotificationMode = NotificationMode.ALL

    @Column(name = "muted_until")
    var mutedUntil: OffsetDateTime? = null

    @Column(name = "archived")
    var archived: Boolean = false

    @Column(name = "pinned")
    var pinned: Boolean = false

    @Column(name = "enabled")
    var enabled: Boolean = false

    enum class NotificationMode {
        ALL, ONLY_MENTIONS, NONE
    }
}