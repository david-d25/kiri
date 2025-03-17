package space.davids_digital.kiri.model.telegram

/**
 * This object represents a service message about the completion of a giveaway without public winners.
 */
data class TelegramGiveawayCompleted (
    val winnerCount: Int,
    val unclaimedPrizeCount: Int? = null,
    /**
     * Message with the giveaway that was completed, if it wasn't deleted.
     */
    val message: TelegramMessage? = null,
    /**
     * True, if the giveaway is a Telegram Star giveaway.
     * Otherwise, currently, the giveaway is a Telegram Premium giveaway.
     */
    val isStarGiveaway: Boolean = true,
)