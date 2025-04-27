package space.davids_digital.kiri.model

import java.time.ZonedDateTime
import java.util.*

data class UserSession(
    val id: UUID,
    val userId: Long,
    val token: String,
    val validUntil: ZonedDateTime?,
    val firstName: String,
    val lastName: String?,
    val username: String?,
    val photoUrl: String?,
    val authDate: ZonedDateTime,
    val hash: String,
)
