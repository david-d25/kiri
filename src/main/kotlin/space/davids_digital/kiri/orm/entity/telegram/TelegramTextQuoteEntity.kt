package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_text_quotes")
class TelegramTextQuoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "text")
    var text: String = ""

    @Column(name = "position")
    var position: Int = 0

    @Column(name = "is_manual")
    var manual: Boolean = false

    @OneToMany(mappedBy = "parentTextQuote", cascade = [CascadeType.ALL], orphanRemoval = true)
    var entities: MutableList<TelegramMessageEntityEntity> = mutableListOf()
}
