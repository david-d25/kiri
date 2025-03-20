package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_inline_keyboard_buttons_cross_links")
class TelegramInlineKeyboardButtonPlacementEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @ManyToOne
    @JoinColumn(name = "markup_id", referencedColumnName = "internal_id", nullable = false)
    var markup: TelegramInlineKeyboardMarkupEntity? = null

    @ManyToOne
    @JoinColumn(name = "button_id", referencedColumnName = "internal_id", nullable = false)
    var button: TelegramInlineKeyboardButtonEntity? = null

    @Column(name = "row_index", nullable = false)
    var rowIndex: Int = 0

    @Column(name = "column_index", nullable = false)
    var columnIndex: Int = 0
}
