package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_locations")
class TelegramLocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "latitude")
    var latitude: Float = 0f

    @Column(name = "longitude")
    var longitude: Float = 0f

    @Column(name = "horizontal_accuracy")
    var horizontalAccuracy: Float? = null

    @Column(name = "live_period")
    var livePeriod: Int? = null

    @Column(name = "heading")
    var heading: Int? = null

    @Column(name = "proximity_alert_radius")
    var proximityAlertRadius: Int? = null
}
