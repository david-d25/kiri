package space.davids_digital.kiri.model.telegram

/**
 * Represents an [inline keyboard](https://core.telegram.org/bots/features#inline-keyboards) that appears right next to
 * the message it belongs to.
 */
data class TelegramInlineKeyboardMarkup (
    /**
     * Array of button rows, each represented by an Array of [TelegramInlineKeyboardButton] objects
     */
    val inlineKeyboard: List<List<TelegramInlineKeyboardButton>>,
)