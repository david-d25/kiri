package space.davids_digital.kiri.model

import java.time.ZonedDateTime
import java.util.UUID

data class MemoryLink(
    val memoryKeyId: UUID,
    val memoryPointId: UUID,
    val weight: Double,
    val lastUpdatedAt: ZonedDateTime
)