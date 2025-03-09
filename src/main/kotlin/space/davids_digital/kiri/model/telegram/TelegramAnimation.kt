package space.davids_digital.kiri.model.telegram

/**
 * Represents an animation file (GIF or H.264/MPEG-4 AVC video without sound).
 *
 * [Reference](https://core.telegram.org/bots/api#animation)
 */
data class TelegramAnimation (
    /**
     * Identifier for this file, which can be used to download or reuse the file.
     */
    val fileId: String,
    /**
     * 	Unique identifier for this file, which is supposed to be the same over time and for different bots.
     * 	Can't be used to download or reuse the file.
     */
    val fileUniqueId: String,
    /**
     * Video width as defined by the sender.
     */
    val width: Int,
    /**
     * Video height as defined by the sender.
     */
    val height: Int,
    /**
     * Duration of the video in seconds as defined by the sender.
     */
    val duration: Int,
    /**
     * Animation thumbnail as defined by the sender
     */
    val thumbnail: TelegramPhotoSize? = null,
    /**
     * Original animation filename as defined by the sender.
     */
    val fileName: String? = null,
    /**
     * MIME type of the file as defined by the sender
     */
    val mimeType: String? = null,
    /**
     * File size in bytes.
     */
    val fileSize: Long? = null,
)