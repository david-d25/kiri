package space.davids_digital.kiri.model

import java.time.ZonedDateTime
import java.util.UUID

data class MemoryPoint(
    val id: UUID,
    val value: String,
    val createdAt: ZonedDateTime
)