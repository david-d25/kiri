package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_link_preview_options")
class TelegramLinkPreviewOptionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "is_disabled")
    var isDisabled: Boolean = false

    @Column(name = "url")
    var url: String? = null

    @Column(name = "prefer_small_media")
    var preferSmallMedia: Boolean = false

    @Column(name = "prefer_large_media")
    var preferLargeMedia: Boolean = false

    @Column(name = "show_above_text")
    var showAboveText: Boolean = false
}
