package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "chat_boosts_added")
class TelegramChatBoostAddedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "boost_count", nullable = false)
    var boostCount: Int = 0
}
