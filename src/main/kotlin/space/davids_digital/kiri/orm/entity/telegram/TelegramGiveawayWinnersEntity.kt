package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramGiveawayWinnersEntityId
import java.time.OffsetDateTime

@Entity
@Table(schema = "telegram", name = "giveaway_winners")
class TelegramGiveawayWinnersEntity {
    @EmbeddedId
    var id: TelegramGiveawayWinnersEntityId = TelegramGiveawayWinnersEntityId()

    @Column(name = "winners_selection_date")
    var winnersSelectionDate: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "winner_count")
    var winnerCount: Int = 0

    @ElementCollection
    @CollectionTable(
        schema = "telegram",
        name = "giveaway_winner_user_ids",
        joinColumns = [
            JoinColumn(name = "chat_id", referencedColumnName = "chat_id"),
            JoinColumn(name = "giveaway_message_id", referencedColumnName = "giveaway_message_id")
        ]
    )
    @Column(name = "user_id")
    var winners: MutableList<Long> = mutableListOf()

    @Column(name = "additional_chat_count")
    var additionalChatCount: Int? = null

    @Column(name = "prize_star_count")
    var prizeStarCount: Int? = null

    @Column(name = "premium_subscription_month_count")
    var premiumSubscriptionMonthCount: Int? = null

    @Column(name = "unclaimed_prize_count")
    var unclaimedPrizeCount: Int? = null

    @Column(name = "only_new_members")
    var onlyNewMembers: Boolean = false

    @Column(name = "was_refunded")
    var wasRefunded: Boolean = false

    @Column(name = "prize_description")
    var prizeDescription: String? = null
}
