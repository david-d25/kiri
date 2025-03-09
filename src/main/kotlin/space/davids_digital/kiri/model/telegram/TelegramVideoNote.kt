package space.davids_digital.kiri.model.telegram

/**
 * Represents a [video message](https://telegram.org/blog/video-messages-and-telescope).
 */
data class TelegramVideoNote (
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
     * Video width and height (diameter of the video message) as defined by the sender.
     */
    val length: Int,
    /**
     * Duration of the video in seconds as defined by the sender.
     */
    val duration: Int,
    val thumbnail: TelegramPhotoSize? = null,
    /**
     * File size in bytes.
     */
    val fileSize: Long? = null,
)