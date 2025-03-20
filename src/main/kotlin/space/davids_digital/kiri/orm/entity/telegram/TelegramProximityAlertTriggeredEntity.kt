package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_proximity_alerts_triggered")
class TelegramProximityAlertTriggeredEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "traveler_id", nullable = false)
    var travelerId: Long = 0

    @Column(name = "watcher_id", nullable = false)
    var watcherId: Long = 0

    @Column(name = "distance", nullable = false)
    var distance: Int = 0
}
