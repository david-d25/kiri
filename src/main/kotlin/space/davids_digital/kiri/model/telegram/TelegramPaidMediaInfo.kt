package space.davids_digital.kiri.model.telegram

/**
 * Represents paid media added to a message.
 */
data class TelegramPaidMediaInfo (
    /**
     * The number of Telegram Stars that must be paid to buy access to the media.
     */
    val starCount: Int,
    /**
     * Information about the paid media.
     */
    val paidMedia: Array<TelegramPaidMedia>,
)