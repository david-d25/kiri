package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Represents a change of a reaction on a message performed by a user.
 */
data class TelegramMessageReactionUpdated(
    /**
     * The chat containing the message the user reacted to
     */
    val chat: TelegramChat,

    /**
     * Unique identifier of the message inside the chat
     */
    val messageId: Int,

    /**
     * The user that changed the reaction, if the user isn't anonymous
     */
    val user: TelegramUser? = null,

    /**
     * The chat on behalf of which the reaction was changed, if the user is anonymous
     */
    val actorChat: TelegramChat? = null,

    /**
     * Date of the change
     */
    val date: ZonedDateTime,

    /**
     * Previous list of reaction types that were set by the user
     */
    val oldReaction: List<TelegramReactionType> = emptyList(),

    /**
     * New list of reaction types that have been set by the user
     */
    val newReaction: List<TelegramReactionType> = emptyList(),
)
