package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_order_info")
class TelegramOrderInfoEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "name")
    var name: String? = null

    @Column(name = "phone_number")
    var phoneNumber: String? = null

    @Column(name = "email")
    var email: String? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "shipping_address_id", referencedColumnName = "internal_id")
    var shippingAddress: TelegramShippingAddressEntity? = null
}
