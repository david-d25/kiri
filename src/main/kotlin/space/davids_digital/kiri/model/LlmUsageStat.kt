package space.davids_digital.kiri.model

import java.time.ZonedDateTime
import java.util.UUID

data class LlmUsageStat(
    val id: UUID,
    val timestamp: ZonedDateTime,
    val provider: String,
    val model: String,
    val inputTokens: Long,
    val outputTokens: Long,
    val cacheReadInputTokens: Long,
    val cacheCreationInputTokens: Long,
    val durationMs: Long,
)
