package space.davids_digital.kiri.model.telegram

/**
 * [Reference](https://core.telegram.org/bots/api#sticker)
 */
data class TelegramSticker (
    /**
     * Identifier for this file, which can be used to download or reuse the file.
     */
    val fileId: String,
    /**
     * Unique identifier for this file, which is supposed to be the same over time and for different bots.
     * Can't be used to download or reuse the file.
     */
    val fileUniqueId: String,
    /**
     * Type of the sticker. The type of the sticker is independent of its format, which is determined by the fields
     * [isAnimated] and [isVideo].
     */
    val type: Type,
    val width: Int,
    val height: Int,
    /**
     * True, if the sticker is [animated](https://telegram.org/blog/animated-stickers).
     */
    val isAnimated: Boolean,
    /**
     * True, if the sticker is a [video sticker](https://telegram.org/blog/video-stickers-better-reactions).
     */
    val isVideo: Boolean,
    /**
     * Sticker thumbnail in the .WEBP or .JPG format.
     */
    val thumbnail: TelegramPhotoSize? = null,
    /**
     * Emoji associated with the sticker.
     */
    val emoji: String? = null,
    /**
     * Name of the sticker set to which the sticker belongs.
     */
    val setName: String? = null,
    /**
     * For premium regular stickers, premium animation for the sticker.
     */
    val premiumAnimation: TelegramFile? = null,
    /**
     * For mask stickers, the position where the mask should be placed.
     */
    val maskPosition: TelegramMaskPosition? = null,
    /**
     * For custom emoji stickers, unique identifier of the custom emoji.
     */
    val customEmojiId: String? = null,
    /**
     * True, if the sticker must be repainted to a text color in messages, the color of the Telegram Premium badge in
     * emoji status, white color on chat photos, or another appropriate color in other places.
     */
    val needsRepainting: Boolean? = null,
    /**
     * File size in bytes.
     */
    val fileSize: Long? = null,
) {
    enum class Type {
        REGULAR,
        MASK,
        CUSTOM_EMOJI,
        UNKNOWN
    }
}