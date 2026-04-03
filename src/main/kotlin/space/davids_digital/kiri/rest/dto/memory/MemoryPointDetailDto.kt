package space.davids_digital.kiri.rest.dto.memory

import java.time.ZonedDateTime
import java.util.UUID

data class MemoryPointDetailDto(
    val id: UUID,
    val value: String,
    val createdAt: ZonedDateTime,
    val links: List<MemoryLinkDto>
)
