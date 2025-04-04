package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "inline_keyboard_buttons_cross_links")
class TelegramInlineKeyboardButtonPlacementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @ManyToOne
    @JoinColumn(name = "markup_id", referencedColumnName = "internal_id", nullable = false)
    var parentMarkup: TelegramInlineKeyboardMarkupEntity? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "button_id", referencedColumnName = "internal_id", nullable = false)
    var button: TelegramInlineKeyboardButtonEntity? = null

    @Column(name = "row_index", nullable = false)
    var rowIndex: Int = 0

    @Column(name = "column_index", nullable = false)
    var columnIndex: Int = 0
}
