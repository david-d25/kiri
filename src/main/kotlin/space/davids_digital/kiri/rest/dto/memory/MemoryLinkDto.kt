package space.davids_digital.kiri.rest.dto.memory

import java.time.ZonedDateTime
import java.util.UUID

data class MemoryLinkDto(
    val memoryKeyId: UUID,
    val memoryPointId: UUID,
    val weight: Double,
    val lastUpdatedAt: ZonedDateTime,
    val keyText: String? = null,
    val pointValue: String? = null
)
