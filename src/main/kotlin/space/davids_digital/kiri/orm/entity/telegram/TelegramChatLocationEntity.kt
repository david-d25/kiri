package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "chat_locations")
class TelegramChatLocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    @JoinColumn(name = "location_id", referencedColumnName = "internal_id")
    var location: TelegramLocationEntity? = null

    @Column(name = "address", nullable = false)
    var address: String = ""
}
