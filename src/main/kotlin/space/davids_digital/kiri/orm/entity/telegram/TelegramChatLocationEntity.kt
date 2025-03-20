package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_chat_locations")
class TelegramChatLocationEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "location_id", referencedColumnName = "internal_id")
    var location: TelegramLocationEntity? = null

    @Column(name = "address", nullable = false)
    var address: String = ""
}
