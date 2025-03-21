package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_stickers")
class TelegramStickerEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "file_download_id")
    var fileDownloadId: String = ""

    @Column(name = "type")
    var type: String = "unknown" // REGULAR, MASK, CUSTOM_EMOJI, UNKNOWN

    @Column(name = "width")
    var width: Int = 0

    @Column(name = "height")
    var height: Int = 0

    @Column(name = "is_animated")
    var animated: Boolean = false

    @Column(name = "is_video")
    var video: Boolean = false

    @ManyToOne
    @JoinColumn(name = "thumbnail_id", referencedColumnName = "file_unique_id")
    var thumbnail: TelegramPhotoSizeEntity? = null

    @Column(name = "emoji")
    var emoji: String? = null

    @Column(name = "set_name")
    var setName: String? = null

    @ManyToOne
    @JoinColumn(name = "premium_animation_file_id", referencedColumnName = "file_unique_id")
    var premiumAnimation: TelegramFileEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "mask_position_id", referencedColumnName = "internal_id")
    var maskPosition: TelegramMaskPositionEntity? = null

    @Column(name = "custom_emoji_id")
    var customEmojiId: String? = null

    @Column(name = "needs_repainting")
    var needsRepainting: Boolean? = null

    @Column(name = "file_size")
    var fileSize: Long? = null
}
