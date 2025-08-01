package space.davids_digital.kiri.model.telegram

data class TelegramBusinessMessagesDeleted (
    /**
     * Unique identifier of the business connection
     */
    val businessConnectionId: String,

    /**
     * Information about a chat in the business account.
     * The bot may not have access to the chat or the corresponding user.
     */
    val chatId: Long,

    /**
     * The list of identifiers of deleted messages in the chat of the business account
     */
    val messageIds: List<Long>,
)