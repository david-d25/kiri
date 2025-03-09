package space.davids_digital.kiri.model.telegram

/**
 * Represents a voice message in Telegram.
 */
data class TelegramVoice (
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
     * Duration of the audio in seconds as defined by the sender.
     */
    val duration: Int,
    /**
     * MIME type of the file as defined by the sender.
     */
    val mimeType: String? = null,
    /**
     * File size in bytes.
     */
    val fileSize: Long? = null
)