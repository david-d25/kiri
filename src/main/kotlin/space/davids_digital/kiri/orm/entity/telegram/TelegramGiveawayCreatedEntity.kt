package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_giveaways_created")
class TelegramGiveawayCreatedEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "prize_star_count")
    var prizeStarCount: Int? = null
}
