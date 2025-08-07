package space.davids_digital.kiri.orm.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "main", name = "user_sessions")
class UserSessionEntity {
    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()

    @Column(name = "user_id")
    var userId: Long = 0

    @Column(name = "token_encrypted")
    var tokenEncrypted: ByteArray = ByteArray(0)

    @Column(name = "valid_until")
    var validUntil: OffsetDateTime? = null

    @Column(name = "first_name")
    var firstName: String = ""

    @Column(name = "last_name")
    var lastName: String? = null

    @Column(name = "username")
    var username: String? = null

    @Column(name = "photo_url")
    var photoUrl: String? = null

    @Column(name = "auth_date")
    var authDate: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "hash")
    var hash: String = ""
}
