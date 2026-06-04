package space.davids_digital.kiri.orm.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.CalendarEventEntity
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface CalendarEventRepository : JpaRepository<CalendarEventEntity, UUID> {

    @Query("""
        select e from CalendarEventEntity e
        where e.enabled = true
          and e.nextFireAt is not null
          and e.nextFireAt <= :now
        order by e.nextFireAt asc
    """)
    fun findDue(@Param("now") now: OffsetDateTime, pageable: Pageable): List<CalendarEventEntity>

    fun findByEnabledTrueAndNextFireAtBetween(
        start: OffsetDateTime,
        end: OffsetDateTime,
        pageable: Pageable
    ): Page<CalendarEventEntity>

    fun findByTitleContainingIgnoreCase(query: String, pageable: Pageable): Page<CalendarEventEntity>

    @Query(
        value = "select id from main.calendar_events where replace(id::text, '-', '') like :prefix || '%'",
        nativeQuery = true
    )
    fun findIdsByHexPrefix(@Param("prefix") prefix: String): List<UUID>
}
