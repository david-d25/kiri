package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "web_app_data")
class TelegramWebAppDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "data", nullable = false)
    var data: String = ""

    @Column(name = "button_text", nullable = false)
    var buttonText: String = ""
}
