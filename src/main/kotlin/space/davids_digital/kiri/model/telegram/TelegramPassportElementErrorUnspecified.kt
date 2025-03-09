package space.davids_digital.kiri.model.telegram

/**
 * Represents an issue in an unspecified place. The error is considered resolved when new data is added.
 */
data class TelegramPassportElementErrorUnspecified (
    /**
     * Error source, must be _unspecified_.
     */
    val source: String,
    /**
     * Type of element of the user's Telegram Passport which has the issue.
     */
    val type: String,
    val elementHashBase64: String,
    /**
     * Error message.
     */
    val message: String,
)