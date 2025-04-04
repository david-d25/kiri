package space.davids_digital.kiri.orm.entity

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(schema = "kiri", name = "user_sessions")
class UserSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Column(name = "user_id")
    var userId: Long = 0

    @Column(name = "session_token_encrypted")
    var sessionTokenEncrypted: ByteArray = ByteArray(0)

    @Column(name = "valid_until")
    var validUntil: ZonedDateTime = ZonedDateTime.now()
}
