package space.davids_digital.kiri.model.telegram

/**
 * Represents one size of a photo or a file / sticker thumbnail.
 * TODO links
 */
data class TelegramPhotoSize (
    /**
     * Identifier for this file, which can be used to download or reuse the file.
     */
    val fileId: String,
    /**
     * Unique identifier for this file, which is supposed to be the same over time and for different bots.
     * Can't be used to download or reuse the file.
     */
    val fileUniqueId: String,
    val width: Int,
    val height: Int,
    /**
     * File size in bytes.
     */
    val fileSize: Long? = null,
)