package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "telegram_successful_payments")
class TelegramSuccessfulPaymentEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "currency", nullable = false)
    var currency: String = ""

    @Column(name = "total_amount", nullable = false)
    var totalAmount: Int = 0

    @Column(name = "invoice_payload", nullable = false)
    var invoicePayload: String = ""

    @Column(name = "subscription_expiration_date")
    var subscriptionExpirationDate: OffsetDateTime? = null

    @Column(name = "is_recurring", nullable = false)
    var isRecurring: Boolean = false

    @Column(name = "is_first_recurring", nullable = false)
    var isFirstRecurring: Boolean = false

    @Column(name = "shipping_option_id")
    var shippingOptionId: String? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "order_info_id", referencedColumnName = "internal_id")
    var orderInfo: TelegramOrderInfoEntity? = null

    @Column(name = "telegram_payment_charge_id", nullable = false)
    var telegramPaymentChargeId: String = ""

    @Column(name = "provider_payment_charge_id", nullable = false)
    var providerPaymentChargeId: String = ""
}
