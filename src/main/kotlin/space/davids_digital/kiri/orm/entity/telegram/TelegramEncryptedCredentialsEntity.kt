package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

// TODO encrypt?
@Entity
@Table(name = "telegram_encrypted_credentials")
class TelegramEncryptedCredentialsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "data_base64", nullable = false)
    var dataBase64: String = ""

    @Column(name = "hash_base64", nullable = false)
    var hashBase64: String = ""

    @Column(name = "secret_base64", nullable = false)
    var secretBase64: String = ""
}
