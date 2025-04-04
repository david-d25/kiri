package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "inline_keyboard_markups")
class TelegramInlineKeyboardMarkupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @OneToMany(mappedBy = "parentMarkup", cascade = [CascadeType.ALL], orphanRemoval = true)
    var buttons: MutableList<TelegramInlineKeyboardButtonPlacementEntity> = mutableListOf()
}
