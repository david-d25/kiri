package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_invoices")
class TelegramInvoiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "title")
    var title: String = ""

    @Column(name = "description")
    var description: String = ""

    @Column(name = "start_parameter")
    var startParameter: String = ""

    @Column(name = "currency")
    var currency: String = ""

    @Column(name = "total_amount")
    var totalAmount: Long = 0
}
