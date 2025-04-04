package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "web_app_info")
class TelegramWebAppInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "url", nullable = false)
    var url: String = ""
}
