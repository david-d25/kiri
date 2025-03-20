package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_login_urls")
class TelegramLoginUrlEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "url", nullable = false)
    var url: String = ""

    @Column(name = "forward_text")
    var forwardText: String? = null

    @Column(name = "bot_username")
    var botUsername: String? = null

    @Column(name = "request_write_access")
    var requestWriteAccess: Boolean? = null
}
