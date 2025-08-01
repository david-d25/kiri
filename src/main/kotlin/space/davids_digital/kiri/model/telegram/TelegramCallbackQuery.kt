package space.davids_digital.kiri.model.telegram

/**
 * Represents an incoming callback query from a callback button in an
 * [inline keyboard](https://core.telegram.org/bots/features#inline-keyboards).
 * If the button that originated the query was attached to a message sent by the bot,
 * the field [message] will be present. If the button was attached to a message sent via the bot
 * (in [inline mode](https://core.telegram.org/bots/api#inline-mode)), the field [inlineMessageId] will be present.
 * Exactly one of the fields [data] or [gameShortName] will be present.
 */
data class TelegramCallbackQuery (
    /**
     * Unique identifier for this query
     */
    val id: String,

    /**
     * Sender
     */
    val from: TelegramUser,

    /**
     * Message sent by the bot with the callback button that originated the query.
     */
    val message: TelegramMaybeInaccessibleMessage? = null,

    /**
     * Identifier of the message sent via the bot in inline mode, that originated the query.
     */
    val inlineMessageId: String? = null,

    /**
     * Global identifier, uniquely corresponding to the chat to which the message with the callback button was sent.
     * Useful for high scores in [games](https://core.telegram.org/bots/api#games).
     */
    val chatInstance: String,

    /**
     * Data associated with the callback button.
     * Be aware that the message originated the query can contain no callback buttons with this data.
     */
    val data: String? = null,

    /**
     * Short name of a [Game](https://core.telegram.org/bots/api#games) to be returned,
     * serves as the unique identifier for the game.
     */
    val gameShortName: String? = null
)