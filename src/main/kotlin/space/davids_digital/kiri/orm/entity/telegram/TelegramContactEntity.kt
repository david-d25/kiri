package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "contacts")
class TelegramContactEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "phone_number")
    var phoneNumber: String = ""

    @Column(name = "first_name")
    var firstName: String = ""

    @Column(name = "last_name")
    var lastName: String? = null

    @Column(name = "user_id")
    var userId: Long? = null

    @Column(name = "vcard")
    var vcard: String? = null
}
