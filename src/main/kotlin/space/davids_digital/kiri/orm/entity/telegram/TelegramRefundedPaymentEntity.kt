package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "refunded_payments")
class TelegramRefundedPaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "currency", nullable = false)
    var currency: String = ""

    @Column(name = "total_amount", nullable = false)
    var totalAmount: Int = 0

    @Column(name = "invoice_payload", nullable = false)
    var invoicePayload: String = ""

    @Column(name = "telegram_payment_charge_id", nullable = false)
    var telegramPaymentChargeId: String = ""

    @Column(name = "provider_payment_charge_id")
    var providerPaymentChargeId: String? = null
}
