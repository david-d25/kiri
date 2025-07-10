package space.davids_digital.kiri.model.telegram

/**
 * Represents an incoming inline query.
 * When the user sends an empty query, your bot could return some default or trending results.
 */
data class TelegramInlineQuery(
    /**
     * Unique identifier for this query
     */
    val id: String,

    /**
     * Sender
     */
    val from: TelegramUser,

    /**
     * Text of the query (up to 256 characters)
     */
    val query: String,

    /**
     * Offset of the results to be returned, can be controlled by the bot
     */
    val offset: String,

    /**
     * Type of the chat from which the inline query was sent. Can be either “sender” for a private chat with the inline
     * query sender, “private”, “group”, “supergroup”, or “channel”. The chat type should be always known for requests
     * sent from official clients and most third-party clients, unless the request was sent from a secret chat
     */
    val chatType: String? = null,

    /**
     * Sender location, only for bots that request user location
     */
    val location: TelegramLocation? = null,
)
