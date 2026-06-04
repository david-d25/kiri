package space.davids_digital.kiri.orm.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import space.davids_digital.kiri.model.CalendarEvent
import space.davids_digital.kiri.orm.entity.CalendarEventEntity
import space.davids_digital.kiri.orm.mapper.CalendarEventEntityMapper
import space.davids_digital.kiri.orm.repository.CalendarEventRepository
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.UUID

@Service
class CalendarOrmService(
    private val repository: CalendarEventRepository,
    private val mapper: CalendarEventEntityMapper,
) {
    @Transactional(readOnly = true)
    fun get(id: UUID): CalendarEvent? =
        mapper.toModel(repository.findById(id).orElse(null))

    @Transactional(readOnly = true)
    fun findDue(now: ZonedDateTime, limit: Int = 100): List<CalendarEvent> =
        repository.findDue(now.toOffsetDateTime(), PageRequest.of(0, limit))
            .mapNotNull(mapper::toModel)

    @Transactional(readOnly = true)
    fun list(page: Int, size: Int): Page<CalendarEvent> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nextFireAt"))
        return repository.findAll(pageable).map { mapper.toModel(it)!! }
    }

    @Transactional(readOnly = true)
    fun searchByTitle(query: String, page: Int, size: Int): Page<CalendarEvent> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nextFireAt"))
        return repository.findByTitleContainingIgnoreCase(query, pageable)
            .map { mapper.toModel(it)!! }
    }

    @Transactional(readOnly = true)
    fun findInRange(from: ZonedDateTime, to: ZonedDateTime, page: Int, size: Int): Page<CalendarEvent> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nextFireAt"))
        return repository.findByEnabledTrueAndNextFireAtBetween(
            from.toOffsetDateTime(), to.toOffsetDateTime(), pageable
        ).map { mapper.toModel(it)!! }
    }

    @Transactional(readOnly = true)
    fun findIdsByHexPrefix(prefix: String): List<UUID> {
        if (prefix.isBlank()) return emptyList()
        return repository.findIdsByHexPrefix(prefix.lowercase())
    }

    @Transactional
    fun create(model: CalendarEvent): CalendarEvent {
        val entity = mapper.toEntity(model) ?: error("Failed to map event for creation")
        entity.updatedAt = OffsetDateTime.now()
        return mapper.toModel(repository.save(entity))!!
    }

    @Transactional
    fun update(model: CalendarEvent): CalendarEvent {
        val existing = repository.findById(model.id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar event not found: ${model.id}")
        }
        existing.title = model.title
        existing.description = model.description
        existing.timezone = model.timezone
        existing.firesAt = model.firesAt?.toOffsetDateTime()
        existing.rrule = model.rrule
        existing.dtstart = model.dtstart?.toOffsetDateTime()
        existing.exdates = model.exdates.map { it.toOffsetDateTime() }.toTypedArray()
        existing.nextFireAt = model.nextFireAt?.toOffsetDateTime()
        existing.lastFiredAt = model.lastFiredAt?.toOffsetDateTime()
        existing.wakeAgent = model.wakeAgent
        existing.missedPolicy = CalendarEventEntity.MissedPolicy.valueOf(model.missedPolicy.name)
        existing.enabled = model.enabled
        existing.updatedAt = OffsetDateTime.now()
        return mapper.toModel(repository.save(existing))!!
    }

    @Transactional
    fun delete(id: UUID) {
        repository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun count(): Long = repository.count()
}
