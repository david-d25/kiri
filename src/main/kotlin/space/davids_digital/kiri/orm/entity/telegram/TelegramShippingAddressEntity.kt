package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_shipping_addresses")
class TelegramShippingAddressEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "country_code", nullable = false)
    var countryCode: String = ""

    @Column(name = "state", nullable = false)
    var state: String = ""

    @Column(name = "city", nullable = false)
    var city: String = ""

    @Column(name = "street_line1", nullable = false)
    var streetLine1: String = ""

    @Column(name = "street_line2", nullable = false)
    var streetLine2: String = ""

    @Column(name = "postcode", nullable = false)
    var postcode: String = ""
}
