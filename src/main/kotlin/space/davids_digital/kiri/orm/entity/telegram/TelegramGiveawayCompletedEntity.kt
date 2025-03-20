package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramGiveawayCompletedId

@Entity
@Table(name = "telegram_giveaways_completed")
class TelegramGiveawayCompletedEntity {

    @EmbeddedId
    var id: TelegramGiveawayCompletedId = TelegramGiveawayCompletedId()

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
