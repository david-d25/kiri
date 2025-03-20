package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "telegram_giveaways")
class TelegramGiveawayEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "winners_selection_date")
    var winnersSelectionDate: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "winner_count")
    var winnerCount: Int = 0

    @Column(name = "only_new_members")
    var onlyNewMembers: Boolean = false

    @Column(name = "has_public_winners")
    var hasPublicWinners: Boolean = false

    @Column(name = "prize_description")
    var prizeDescription: String? = null

    @Column(name = "prize_star_count")
    var prizeStarCount: Int? = null

    @Column(name = "premium_subscription_month_count")
    var premiumSubscriptionMonthCount: Int? = null

    @ElementCollection
    @CollectionTable(
        name = "telegram_giveaway_country_codes",
        joinColumns = [JoinColumn(name = "giveaway_id", referencedColumnName = "internal_id")]
    )
    @Column(name = "country_code")
    var countryCodes: MutableList<String> = mutableListOf()

    @ElementCollection
    @CollectionTable(
        name = "telegram_giveaway_chat_ids",
        joinColumns = [JoinColumn(name = "giveaway_id", referencedColumnName = "internal_id")]
    )
    @Column(name = "chat_id")
    var chatIds: MutableList<Long> = mutableListOf()
}
