package space.davids_digital.kiri.model.telegram

data class TelegramVideo (
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
     * Video width as defined by sender.
     */
    val width: Int,
    /**
     * Video height as defined by sender.
     */
    val height: Int,
    /**
     * Duration of the video in seconds as defined by sender.
     */
    val duration: Int,
    val thumbnail: TelegramPhotoSize? = null,
    /**
     * Available sizes of the cover of the video in the message.
     */
    val cover: Array<TelegramPhotoSize>? = null,
    /**
     * Timestamp in seconds from which the video will play in the message.
     */
    val startTimestamp: Int? = null,
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