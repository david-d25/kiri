package space.davids_digital.kiri.model.telegram

/**
 * Describes a message that was deleted or is otherwise inaccessible to the bot.
 */
data class TelegramInaccessibleMessage(
    /**
     * Chat the message belonged to.
     */
    val chatId: Long,
    /**
     * Unique message identifier inside the chat.
     */
    val messageId: Long,
): TelegramMaybeInaccessibleMessage()