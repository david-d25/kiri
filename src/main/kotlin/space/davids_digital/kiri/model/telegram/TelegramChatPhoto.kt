package space.davids_digital.kiri.model.telegram

/**
 * Telegram chat photo model.
 *
 * [Reference](https://core.telegram.org/bots/api#chatphoto)
 */
data class TelegramChatPhoto (
    /**
     * File identifier of small (160x160) chat photo.
     * This file_id can be used only for photo download and only for as long as the photo is not changed.
     */
    val smallFileId: String,
    /**
     * Unique file identifier of small (160x160) chat photo, which is supposed to be the same over time and for
     * different bots. Can't be used to download or reuse the file.
     */
    val smallFileUniqueId: String,
    /**
     * File identifier of big (640x640) chat photo.
     * This file_id can be used only for photo download and only for as long as the photo is not changed.
     */
    val bigFileId: String,
    /**
     * Unique file identifier of big (640x640) chat photo, which is supposed to be the same over time and for different
     * bots. Can't be used to download or reuse the file.
     */
    val bigFileUniqueId: String,
)