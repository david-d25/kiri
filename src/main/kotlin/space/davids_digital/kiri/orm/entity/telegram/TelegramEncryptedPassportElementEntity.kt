package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_encrypted_passport_elements")
class TelegramEncryptedPassportElementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    lateinit var type: Type

    @Column(name = "data_base64")
    var dataBase64: String? = null

    @Column(name = "phone_number")
    var phoneNumber: String? = null

    @Column(name = "email")
    var email: String? = null

    @Column(name = "hash_base64")
    var hashBase64: String? = null

    @ManyToMany(cascade = [CascadeType.MERGE, CascadeType.PERSIST])
    @JoinTable(
        name = "telegram_encrypted_passport_elements_files_cross_links",
        joinColumns = [JoinColumn(name = "element_id", referencedColumnName = "internal_id")],
        inverseJoinColumns = [JoinColumn(name = "file_id", referencedColumnName = "file_unique_id")]
    )
    var files: MutableList<TelegramPassportFileEntity> = mutableListOf()

    @ManyToMany(cascade = [CascadeType.MERGE, CascadeType.PERSIST])
    @JoinTable(
        name = "telegram_encrypted_passport_elements_translations_cross_links",
        joinColumns = [JoinColumn(name = "element_id", referencedColumnName = "internal_id")],
        inverseJoinColumns = [JoinColumn(name = "file_id", referencedColumnName = "file_unique_id")]
    )
    var translation: MutableList<TelegramPassportFileEntity> = mutableListOf()

    @ManyToOne
    @JoinColumn(name = "front_side_id", referencedColumnName = "file_unique_id")
    var frontSide: TelegramPassportFileEntity? = null

    @ManyToOne
    @JoinColumn(name = "reverse_side_id", referencedColumnName = "file_unique_id")
    var reverseSide: TelegramPassportFileEntity? = null

    @ManyToOne
    @JoinColumn(name = "selfie_id", referencedColumnName = "file_unique_id")
    var selfie: TelegramPassportFileEntity? = null

    enum class Type {
        PERSONAL_DETAILS,
        PASSPORT,
        DRIVER_LICENSE,
        IDENTITY_CARD,
        INTERNAL_PASSPORT,
        ADDRESS,
        UTILITY_BILL,
        BANK_STATEMENT,
        RENTAL_AGREEMENT,
        PASSPORT_REGISTRATION,
        TEMPORARY_REGISTRATION,
        PHONE_NUMBER,
        EMAIL
    }
}
