package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Represents reaction changes on a message with anonymous reactions.
 */
data class TelegramMessageReactionCountUpdated(
    /**
     * The chat containing the message
     */
    val chat: TelegramChat,

    /**
     * Unique message identifier inside the chat
     */
    val messageId: Int,

    /**
     * Date of the change
     */
    val date: ZonedDateTime,

    /**
     * List of reactions that are present on the message
     */
    val reactions: List<TelegramReactionCount> = emptyList()
)
