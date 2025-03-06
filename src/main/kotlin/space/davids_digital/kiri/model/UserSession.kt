package space.davids_digital.kiri.model

import java.time.ZonedDateTime
import java.util.*

data class UserSession(
    val id: UUID,
    val userId: Long,
    val sessionToken: String,
    val validUntil: ZonedDateTime
)
