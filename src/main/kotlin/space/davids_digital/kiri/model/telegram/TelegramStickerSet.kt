package space.davids_digital.kiri.model.telegram

/**
 * This object represents a sticker set.
 */
data class TelegramStickerSet(
    /**
     * Sticker set name
     */
    val name: String,

    /**
     * 	Sticker set title
     */
    val title: String,

    /**
     * Type of stickers in the set
     */
    val type: TelegramSticker.Type,

    /**
     * List of all set stickers
     */
    val stickers: List<TelegramSticker>,

    /**
     * Sticker set thumbnail in the .WEBP, .TGS, or .WEBM format
     */
    val thumbnail: TelegramPhotoSize?
)