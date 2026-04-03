package space.davids_digital.kiri.rest.dto.memory

import java.time.ZonedDateTime
import java.util.UUID

data class MemoryPointDto(
    val id: UUID,
    val value: String,
    val createdAt: ZonedDateTime,
    val linkedKeysCount: Int
)
