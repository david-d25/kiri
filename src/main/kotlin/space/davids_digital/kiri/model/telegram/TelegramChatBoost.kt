package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Contains information about a chat boost.
 */
data class TelegramChatBoost (
    /**
     * Unique identifier of the boost
     */
    val boostId: String,

    /**
     * Point in time when the chat was boosted
     */
    val addDate: ZonedDateTime,

    /**
     * Point in time when the boost will automatically expire, unless the booster's Telegram Premium subscription is
     * prolonged
     */
    val expirationDate: ZonedDateTime,

    /**
     * Source of the added boost
     */
    val source: TelegramChatBoostSource,
)