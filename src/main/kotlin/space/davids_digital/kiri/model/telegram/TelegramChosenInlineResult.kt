package space.davids_digital.kiri.model.telegram

/**
 * Represents a [result](https://core.telegram.org/bots/api#inlinequeryresult) of an inline query that was chosen by
 * the user and sent to their chat partner.
 */
data class TelegramChosenInlineResult (
    /**
     * Unique identifier for the result that was chosen
     */
    val resultId: String,

    /**
     * The user that chose the result
     */
    val from: TelegramUser,

    /**
     * Sender location, only for bots that request user location
     */
    val location: TelegramLocation? = null,

    /**
     * Identifier of the inline message sent. Available only if there is an
     * [inline keyboard](https://core.telegram.org/bots/api#inlinekeyboardmarkup) attached to the message.
     * Will be also received in [callback queries](https://core.telegram.org/bots/api#callbackquery) and can be used to
     * [edit](https://core.telegram.org/bots/api#updating-messages) the message.
     */
    val inlineMessageId: String? = null,

    /**
     * The query that was used to obtain the result
     */
    val query: String,
)