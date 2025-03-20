package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_message_auto_delete_timer_changes")
class TelegramMessageAutoDeleteTimerChangedEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "message_auto_delete_time")
    var messageAutoDeleteTime: Int = 0
}
