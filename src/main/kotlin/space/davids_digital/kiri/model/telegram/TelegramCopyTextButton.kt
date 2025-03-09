package space.davids_digital.kiri.model.telegram

/**
 * Represents an inline keyboard button that copies specified text to the clipboard.
 */
data class TelegramCopyTextButton (
    /**
     * The text to be copied to the clipboard; 1-256 characters.
     */
    val text: String,
)