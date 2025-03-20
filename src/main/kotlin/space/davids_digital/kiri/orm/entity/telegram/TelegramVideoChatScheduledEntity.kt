package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "telegram_video_chat_scheduled")
class TelegramVideoChatScheduledEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "start_date", nullable = false)
    var startDate: OffsetDateTime = OffsetDateTime.now()
}
