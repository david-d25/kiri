package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_write_access_allowed")
class TelegramWriteAccessAllowedEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "from_request", nullable = false)
    var fromRequest: Boolean = false

    @Column(name = "web_app_name")
    var webAppName: String? = null

    @Column(name = "from_attachment_menu", nullable = false)
    var fromAttachmentMenu: Boolean = false
}
