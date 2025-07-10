package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Represents a boost removed from a chat.
 */
data class TelegramChatBoostRemoved(
    /**
     * Chat which was boosted
     */
    val chat: TelegramChat,

    /**
     * Unique identifier of the boost
     */
    val boostId: String,

    /**
     * Point in time when the boost was removed
     */
    val removeDate: ZonedDateTime,

    /**
     * Source of the removed boost
     */
    val source: TelegramChatBoostSource,
)
