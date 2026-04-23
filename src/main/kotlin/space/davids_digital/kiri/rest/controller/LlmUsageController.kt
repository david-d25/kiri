package space.davids_digital.kiri.rest.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.orm.service.LlmUsageStatOrmService
import space.davids_digital.kiri.rest.dto.llmusage.LlmUsageDailyAggregateDto
import space.davids_digital.kiri.rest.dto.llmusage.LlmUsageStatDto
import space.davids_digital.kiri.rest.dto.llmusage.LlmUsageSummaryDto
import java.time.Duration
import java.time.ZonedDateTime

@RestController
@RequestMapping("/llm-usage")
class LlmUsageController(
    private val usageStats: LlmUsageStatOrmService,
) {
    @GetMapping("/recent")
    fun recent(@RequestParam(defaultValue = "50") limit: Int): List<LlmUsageStatDto> {
        val capped = limit.coerceIn(1, 500)
        return usageStats.findRecent(capped).map { stat ->
            LlmUsageStatDto(
                id = stat.id,
                timestamp = stat.timestamp,
                provider = stat.provider,
                model = stat.model,
                inputTokens = stat.inputTokens,
                outputTokens = stat.outputTokens,
                cacheReadInputTokens = stat.cacheReadInputTokens,
                cacheCreationInputTokens = stat.cacheCreationInputTokens,
                durationMs = stat.durationMs,
            )
        }
    }

    @GetMapping("/aggregate")
    fun aggregate(
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?,
    ): List<LlmUsageDailyAggregateDto> {
        val toTs = to?.let { ZonedDateTime.parse(it) } ?: ZonedDateTime.now()
        val fromTs = from?.let { ZonedDateTime.parse(it) } ?: toTs.minusDays(30)
        return usageStats.aggregateByDayAndModel(fromTs, toTs).map { agg ->
            LlmUsageDailyAggregateDto(
                day = agg.day,
                model = agg.model,
                provider = agg.provider,
                inputTokens = agg.inputTokens,
                outputTokens = agg.outputTokens,
                cacheReadInputTokens = agg.cacheReadInputTokens,
                cacheCreationInputTokens = agg.cacheCreationInputTokens,
                requestCount = agg.requestCount,
            )
        }
    }

    @GetMapping("/summary")
    fun summary(): LlmUsageSummaryDto {
        val now = ZonedDateTime.now()
        val windows = listOf(
            "24h" to Duration.ofHours(24),
            "7d" to Duration.ofDays(7),
            "30d" to Duration.ofDays(30),
        )
        val windowDtos = windows.map { (name, duration) ->
            buildWindow(name, now.minus(duration), now)
        }
        val thirtyDay = usageStats.aggregateByModel(now.minusDays(30), now).map { agg ->
            LlmUsageSummaryDto.ModelBreakdown(
                model = agg.model,
                provider = agg.provider,
                inputTokens = agg.inputTokens,
                outputTokens = agg.outputTokens,
                cacheReadInputTokens = agg.cacheReadInputTokens,
                cacheCreationInputTokens = agg.cacheCreationInputTokens,
                requestCount = agg.requestCount,
            )
        }
        return LlmUsageSummaryDto(windows = windowDtos, byModel = thirtyDay)
    }

    private fun buildWindow(name: String, from: ZonedDateTime, to: ZonedDateTime): LlmUsageSummaryDto.Window {
        val byModel = usageStats.aggregateByModel(from, to)
        var inputTokens = 0L
        var outputTokens = 0L
        var cacheReadTokens = 0L
        var cacheCreationTokens = 0L
        var requestCount = 0L
        for (agg in byModel) {
            inputTokens += agg.inputTokens
            outputTokens += agg.outputTokens
            cacheReadTokens += agg.cacheReadInputTokens
            cacheCreationTokens += agg.cacheCreationInputTokens
            requestCount += agg.requestCount
        }
        val rawInput = inputTokens + cacheReadTokens + cacheCreationTokens
        val hitRate = if (rawInput > 0) cacheReadTokens.toDouble() / rawInput else 0.0
        return LlmUsageSummaryDto.Window(
            name = name,
            fromTs = from,
            toTs = to,
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            cacheReadInputTokens = cacheReadTokens,
            cacheCreationInputTokens = cacheCreationTokens,
            requestCount = requestCount,
            rawInputTokens = rawInput,
            cacheHitRate = hitRate,
        )
    }
}
