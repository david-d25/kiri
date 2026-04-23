package space.davids_digital.kiri.orm.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.LlmUsageStat
import space.davids_digital.kiri.orm.entity.LlmUsageStatEntity
import space.davids_digital.kiri.orm.mapper.LlmUsageStatEntityMapper
import space.davids_digital.kiri.orm.repository.LlmUsageStatRepository
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Service
class LlmUsageStatOrmService(
    private val repository: LlmUsageStatRepository,
    private val mapper: LlmUsageStatEntityMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Records a single LLM request's usage. Never throws — logs and swallows.
     * Unknown token counts (-1) are coerced to 0.
     */
    @Transactional
    fun record(
        provider: String,
        model: String,
        inputTokens: Long,
        outputTokens: Long,
        cacheReadInputTokens: Long,
        cacheCreationInputTokens: Long,
        durationMs: Long,
        timestamp: ZonedDateTime = ZonedDateTime.now(),
    ) {
        try {
            val entity = LlmUsageStatEntity().apply {
                this.timestamp = timestamp.toOffsetDateTime()
                this.provider = provider
                this.model = model
                this.inputTokens = inputTokens.coerceAtLeast(0)
                this.outputTokens = outputTokens.coerceAtLeast(0)
                this.cacheReadInputTokens = cacheReadInputTokens.coerceAtLeast(0)
                this.cacheCreationInputTokens = cacheCreationInputTokens.coerceAtLeast(0)
                this.durationMs = durationMs
            }
            repository.save(entity)
        } catch (e: Exception) {
            log.warn("Failed to record LLM usage stat (provider={}, model={})", provider, model, e)
        }
    }

    @Transactional(readOnly = true)
    fun findRecent(limit: Int): List<LlmUsageStat> {
        val pageable = PageRequest.of(0, limit)
        return repository.findAllByOrderByTimestampDesc(pageable).mapNotNull(mapper::toModel)
    }

    /**
     * Aggregated buckets for a [from, to) time range, grouped by (day, model, provider).
     */
    @Transactional(readOnly = true)
    fun aggregateByDayAndModel(from: ZonedDateTime, to: ZonedDateTime): List<DailyModelAggregate> {
        return repository.aggregateByDayAndModel(from.toOffsetDateTime(), to.toOffsetDateTime()).map {
            DailyModelAggregate(
                day = toZonedDateTime(it[0]),
                model = it[1] as String,
                provider = it[2] as String,
                inputTokens = (it[3] as Number).toLong(),
                outputTokens = (it[4] as Number).toLong(),
                cacheReadInputTokens = (it[5] as Number).toLong(),
                cacheCreationInputTokens = (it[6] as Number).toLong(),
                requestCount = (it[7] as Number).toLong(),
            )
        }
    }

    @Transactional(readOnly = true)
    fun aggregateByModel(from: ZonedDateTime, to: ZonedDateTime): List<ModelAggregate> {
        return repository.aggregateByModel(from.toOffsetDateTime(), to.toOffsetDateTime()).map {
            ModelAggregate(
                model = it[0] as String,
                provider = it[1] as String,
                inputTokens = (it[2] as Number).toLong(),
                outputTokens = (it[3] as Number).toLong(),
                cacheReadInputTokens = (it[4] as Number).toLong(),
                cacheCreationInputTokens = (it[5] as Number).toLong(),
                requestCount = (it[6] as Number).toLong(),
            )
        }
    }

    private fun toZonedDateTime(value: Any): ZonedDateTime = when (value) {
        is ZonedDateTime -> value
        is OffsetDateTime -> value.toZonedDateTime()
        // `date_trunc('day', ts at time zone 'UTC')` returns `timestamp without time zone`
        // that JDBC delivers as java.sql.Timestamp. Its wall-clock is the UTC midnight,
        // so we bind the LocalDateTime directly to UTC (avoid toInstant() which would
        // apply the JVM default timezone offset).
        is Timestamp -> value.toLocalDateTime().atZone(ZoneOffset.UTC)
        else -> throw IllegalStateException("Unexpected day type: ${value.javaClass}")
    }

    data class DailyModelAggregate(
        val day: ZonedDateTime,
        val model: String,
        val provider: String,
        val inputTokens: Long,
        val outputTokens: Long,
        val cacheReadInputTokens: Long,
        val cacheCreationInputTokens: Long,
        val requestCount: Long,
    )

    data class ModelAggregate(
        val model: String,
        val provider: String,
        val inputTokens: Long,
        val outputTokens: Long,
        val cacheReadInputTokens: Long,
        val cacheCreationInputTokens: Long,
        val requestCount: Long,
    )
}
