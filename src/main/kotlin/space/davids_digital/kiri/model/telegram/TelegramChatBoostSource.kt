package space.davids_digital.kiri.model.telegram

/**
 * Describes the source of a chat boost.
 */
sealed class TelegramChatBoostSource(
    /**
     * Source of the boost
     */
    val source: String,
) {
    /**
     * The boost was obtained by subscribing to Telegram Premium or by gifting a Telegram Premium subscription to
     * another user.
     */
    data class Premium(
        /**
         * User that boosted the chat
         */
        val user: TelegramUser,
    ): TelegramChatBoostSource("premium")

    /**
     * The boost was obtained by the creation of Telegram Premium gift codes to boost a chat. Each such code boosts the
     * chat 4 times for the duration of the corresponding Telegram Premium subscription.
     */
    data class GiftCode(
        /**
         * User for which the gift code was created
         */
        val user: TelegramUser,
    ): TelegramChatBoostSource("gift_code")

    /**
     * The boost was obtained by the creation of a Telegram Premium or a Telegram Star giveaway. This boosts the chat 4
     * times for the duration of the corresponding Telegram Premium subscription for Telegram Premium giveaways and
     * _prize_star_count_ / 500 times for one year for Telegram Star giveaways.
     */
    data class Giveaway(
        /**
         * Identifier of a message in the chat with the giveaway; the message could have been deleted already. May be 0
         * if the message isn't sent yet.
         */
        val giveawayMessageId: Int,

        /**
         * User that won the prize in the giveaway if any; for Telegram Premium giveaways only
         */
        val user: TelegramUser? = null,

        /**
         * The number of Telegram Stars to be split between giveaway winners; for Telegram Star giveaways only
         */
        val prizeStarCount: Int? = null,

        /**
         * True, if the giveaway was completed, but there was no user to win the prize
         */
        val isUnclaimed: Boolean = false,
    ): TelegramChatBoostSource("giveaway")
}
