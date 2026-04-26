package space.davids_digital.kiri.orm.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.LlmUsageStatEntity
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface LlmUsageStatRepository : JpaRepository<LlmUsageStatEntity, UUID> {

    fun findAllByOrderByTimestampDesc(pageable: Pageable): List<LlmUsageStatEntity>

    /**
     * Aggregate per day × model within [from, to].
     * Returns rows of (day: OffsetDateTime at day-start UTC, model: String, provider: String,
     *                  inputTokens: Long, outputTokens: Long, cacheReadInputTokens: Long,
     *                  cacheCreationInputTokens: Long, requestCount: Long).
     */
    @Query(
        """
            select date_trunc('day', timestamp at time zone 'UTC') as day,
                   model,
                   provider,
                   coalesce(sum(input_tokens), 0)                as input_tokens,
                   coalesce(sum(output_tokens), 0)               as output_tokens,
                   coalesce(sum(cache_read_input_tokens), 0)     as cache_read_input_tokens,
                   coalesce(sum(cache_creation_input_tokens), 0) as cache_creation_input_tokens,
                   count(*)                                      as request_count
            from main.llm_usage_stats
            where timestamp >= :fromTs and timestamp < :toTs
            group by day, model, provider
            order by day asc, model asc
        """,
        nativeQuery = true
    )
    fun aggregateByDayAndModel(
        @Param("fromTs") fromTs: OffsetDateTime,
        @Param("toTs") toTs: OffsetDateTime,
    ): List<Array<Any>>

    /**
     * Aggregate per hour × model within [from, to].
     * Returns rows of (bucket: hour-start UTC, model, provider, ...sums..., requestCount).
     */
    @Query(
        """
            select date_trunc('hour', timestamp at time zone 'UTC') as bucket,
                   model,
                   provider,
                   coalesce(sum(input_tokens), 0)                as input_tokens,
                   coalesce(sum(output_tokens), 0)               as output_tokens,
                   coalesce(sum(cache_read_input_tokens), 0)     as cache_read_input_tokens,
                   coalesce(sum(cache_creation_input_tokens), 0) as cache_creation_input_tokens,
                   count(*)                                      as request_count
            from main.llm_usage_stats
            where timestamp >= :fromTs and timestamp < :toTs
            group by bucket, model, provider
            order by bucket asc, model asc
        """,
        nativeQuery = true
    )
    fun aggregateByHourAndModel(
        @Param("fromTs") fromTs: OffsetDateTime,
        @Param("toTs") toTs: OffsetDateTime,
    ): List<Array<Any>>

    @Query(
        """
            select model,
                   provider,
                   coalesce(sum(input_tokens), 0)                as input_tokens,
                   coalesce(sum(output_tokens), 0)               as output_tokens,
                   coalesce(sum(cache_read_input_tokens), 0)     as cache_read_input_tokens,
                   coalesce(sum(cache_creation_input_tokens), 0) as cache_creation_input_tokens,
                   count(*)                                      as request_count
            from main.llm_usage_stats
            where timestamp >= :fromTs and timestamp < :toTs
            group by model, provider
            order by model asc
        """,
        nativeQuery = true
    )
    fun aggregateByModel(
        @Param("fromTs") fromTs: OffsetDateTime,
        @Param("toTs") toTs: OffsetDateTime,
    ): List<Array<Any>>
}
