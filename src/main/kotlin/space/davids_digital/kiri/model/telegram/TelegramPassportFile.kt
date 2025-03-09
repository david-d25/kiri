package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Represents a file uploaded to Telegram Passport.
 * Currently, all Telegram Passport files are in JPEG format when decrypted and don't exceed 10 MB.
 */
data class TelegramPassportFile (
    /**
     * Identifier for this file, which can be used to download or reuse the file.
     */
    val fileId: String,
    /**
     * Unique identifier for this file, which is supposed to be the same over time and for different bots.
     * Can't be used to download or reuse the file.
     */
    val fileUniqueId: String,
    val fileSizeBytes: Long,
    /**
     * Time when the file was uploaded.
     */
    val fileDate: ZonedDateTime,
)