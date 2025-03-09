package space.davids_digital.kiri.model.telegram

/**
 * Represents a document that can be sent to a Telegram chat.
 */
data class TelegramDocument (
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
     * Document thumbnail as defined by the sender
     */
    val thumbnail: TelegramPhotoSize? = null,
    /**
     * Original filename as defined by the sender.
     */
    val fileName: String? = null,
    /**
     * MIME type of the file as defined by the sender.
     */
    val mimeType: String? = null,
    /**
     * File size in bytes.
     */
    val fileSize: Long? = null,
)