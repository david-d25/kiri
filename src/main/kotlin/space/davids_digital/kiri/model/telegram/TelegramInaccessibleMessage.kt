package space.davids_digital.kiri.model.telegram

/**
 * Describes a message that was deleted or is otherwise inaccessible to the bot.
 */
data class TelegramInaccessibleMessage(
    override val chatId: Long,
    override val messageId: Int,
): TelegramMaybeInaccessibleMessage(chatId, messageId)