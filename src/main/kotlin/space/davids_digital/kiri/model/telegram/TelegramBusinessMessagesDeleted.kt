package space.davids_digital.kiri.model.telegram

/**
 * This object is received when messages are deleted from a connected business account.
 */
data class TelegramBusinessMessagesDeleted (
    /**
     * Unique identifier of the business connection
     */
    val businessConnectionId: String,

    /**
     * Information about a chat in the business account.
     * The bot may not have access to the chat or the corresponding user.
     */
    val chat: TelegramChat,

    /**
     * The list of identifiers of deleted messages in the chat of the business account
     */
    val messageIds: List<Int>,
)