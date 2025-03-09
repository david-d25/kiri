package space.davids_digital.kiri.model.telegram

/**
 * This object represents a service message about the creation of a scheduled giveaway.
 */
data class TelegramGiveawayCreated (
    /**
     * The number of Telegram Stars to be split between giveaway winners; for Telegram Star giveaways only.
     */
    val prizeStarCount: Int? = null,
)