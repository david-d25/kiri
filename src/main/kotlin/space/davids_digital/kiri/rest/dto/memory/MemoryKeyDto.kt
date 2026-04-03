package space.davids_digital.kiri.rest.dto.memory

import java.util.UUID

data class MemoryKeyDto(
    val id: UUID,
    val keyText: String,
    val embeddingModel: String,
    val linkedPointsCount: Int
)
