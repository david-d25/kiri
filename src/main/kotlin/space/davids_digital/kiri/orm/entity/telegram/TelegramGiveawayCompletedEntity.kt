package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "giveaways_completed")
class TelegramGiveawayCompletedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "winner_count", nullable = false)
    var winnerCount: Int = 0

    @Column(name = "unclaimed_prize_count")
    var unclaimedPrizeCount: Int? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "original_message_chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "original_message_message_id", referencedColumnName = "message_id")
    )
    var message: TelegramMessageEntity? = null

    @Column(name = "is_star_giveaway", nullable = false)
    var isStarGiveaway: Boolean = true
}
