package space.davids_digital.kiri.rest.dto.llmusage

import java.time.ZonedDateTime
import java.util.UUID

data class LlmUsageStatDto(
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

data class LlmUsageDailyAggregateDto(
    val day: ZonedDateTime,
    val model: String,
    val provider: String,
    val inputTokens: Long,
    val outputTokens: Long,
    val cacheReadInputTokens: Long,
    val cacheCreationInputTokens: Long,
    val requestCount: Long,
)

data class LlmUsageSummaryDto(
    val windows: List<Window>,
    val byModel: List<ModelBreakdown>,
) {
    data class Window(
        /** e.g. "24h", "7d", "30d". */
        val name: String,
        val fromTs: ZonedDateTime,
        val toTs: ZonedDateTime,
        val inputTokens: Long,
        val outputTokens: Long,
        val cacheReadInputTokens: Long,
        val cacheCreationInputTokens: Long,
        val requestCount: Long,
        /** Fresh + cache_read + cache_creation (total context volume). */
        val rawInputTokens: Long,
        /** cacheReadInputTokens / rawInputTokens, or 0 if rawInputTokens == 0. */
        val cacheHitRate: Double,
    )

    data class ModelBreakdown(
        val model: String,
        val provider: String,
        val inputTokens: Long,
        val outputTokens: Long,
        val cacheReadInputTokens: Long,
        val cacheCreationInputTokens: Long,
        val requestCount: Long,
    )
}
