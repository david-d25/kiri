package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_passport_data")
class TelegramPassportDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "telegram_passport_data_elements_cross_links",
        joinColumns = [JoinColumn(name = "passport_data_id", referencedColumnName = "internal_id")],
        inverseJoinColumns = [JoinColumn(name = "element_id", referencedColumnName = "internal_id")]
    )
    var data: MutableList<TelegramEncryptedPassportElementEntity> = mutableListOf()

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    @JoinColumn(name = "credentials_id", referencedColumnName = "internal_id", nullable = false)
    var credentials: TelegramEncryptedCredentialsEntity? = null
}
