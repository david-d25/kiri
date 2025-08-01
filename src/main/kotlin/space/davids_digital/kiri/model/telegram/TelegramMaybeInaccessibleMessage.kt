package space.davids_digital.kiri.model.telegram

/**
 * Describes a message that can be inaccessible to the bot.
 */
sealed class TelegramMaybeInaccessibleMessage(
    /**
     * Chat the message belonged to.
     */
    open val chatId: Long,
    /**
     * Unique message identifier inside the chat.
     */
    open val messageId: Int,
)