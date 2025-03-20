package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_inline_keyboard_markups")
class TelegramInlineKeyboardMarkupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @OneToMany(mappedBy = "markup", cascade = [CascadeType.ALL], orphanRemoval = true)
    var buttons: MutableList<TelegramInlineKeyboardButtonPlacementEntity> = mutableListOf()
}
