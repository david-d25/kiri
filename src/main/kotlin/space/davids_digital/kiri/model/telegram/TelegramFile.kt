package space.davids_digital.kiri.model.telegram

/**
 * Represents a file ready to be downloaded.
 * The file can be downloaded via the link `https://api.telegram.org/file/bot<token>/<file_path>`.
 * It is guaranteed that the link will be valid for at least 1 hour.
 * When the link expires, a new one can be requested by calling [getFile](https://core.telegram.org/bots/api#getfile).
 *
 * [Reference](https://core.telegram.org/bots/api#file)
 */
data class TelegramFile (
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
     * File size in bytes.
     */
    val fileSize: Long? = null,
    /**
     * File path. Use `https://api.telegram.org/file/bot<token>/<file_path>` to get the file.
     */
    val filePath: String? = null,
)