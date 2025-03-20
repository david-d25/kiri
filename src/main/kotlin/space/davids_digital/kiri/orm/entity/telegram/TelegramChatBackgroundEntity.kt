package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_chat_backgrounds")
class TelegramChatBackgroundEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "background_type_id", referencedColumnName = "internal_id")
    var type: TelegramBackgroundTypeEntity? = null
}
