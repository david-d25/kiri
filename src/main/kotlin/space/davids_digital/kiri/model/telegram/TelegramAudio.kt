package space.davids_digital.kiri.model.telegram

/**
 * Represents an audio file to be treated as music by the Telegram clients.
 *
 * [Reference](https://core.telegram.org/bots/api#audio)
 */
data class TelegramAudio (
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
     * Performer of the audio as defined by the sender or by audio tags.
     */
    val performer: String? = null,
    /**
     * Title of the audio as defined by the sender or by audio tags.
     */
    val title: String? = null,
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
    /**
     * Thumbnail of the album cover to which the music file belongs.
     */
    val thumbnail: TelegramPhotoSize? = null,
)