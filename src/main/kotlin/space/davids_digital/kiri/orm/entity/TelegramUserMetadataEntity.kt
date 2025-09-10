package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(schema = "main", name = "telegram_user_metadata")
class TelegramUserMetadataEntity {
    @Id
    @Column(name = "user_id")
    var userId: Long = 0

    @Column(name = "is_blocked")
    var isBlocked: Boolean = false
}