package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Represents a message about the completion of a giveaway with public winners.
 */
data class TelegramGiveawayWinners(
    /**
     * The chat that created the giveaway.
     */
    val chatId: TelegramChatId,
    /**
     * Identifier of the message with the giveaway in the chat.
     */
    val giveawayMessageId: TelegramMessageId,
    /**
     * Point in time when winners of the giveaway were selected.
     */
    val winnersSelectionDate: ZonedDateTime,
    /**
     * Total number of winners in the giveaway.
     */
    val winnerCount: Int,
    /**
     * List of up to 100 winners of the giveaway.
     */
    val winners: List<TelegramUserId>,
    /**
     * The number of other chats the user had to join in order to be eligible for the giveaway.
     */
    val additionalChatCount: Int? = null,
    /**
     * The number of Telegram Stars that were split between giveaway winners; for Telegram Star giveaways only.
     */
    val prizeStarCount: Int? = null,
    /**
     * The number of months the Telegram Premium subscription won from the giveaway will be active for;
     * for Telegram Premium giveaways only.
     */
    val premiumSubscriptionMonthCount: Int? = null,
    /**
     * Number of undistributed prizes.
     */
    val unclaimedPrizeCount: Int? = null,
    /**
     * True, if only users who had joined the chats after the giveaway started were eligible to win.
     */
    val onlyNewMembers: Boolean? = null,
    /**
     * True, if the giveaway was canceled because the payment for it was refunded.
     */
    val wasRefunded: Boolean? = null,
    /**
     * Description of additional giveaway prize.
     */
    val prizeDescription: String? = null,
)
