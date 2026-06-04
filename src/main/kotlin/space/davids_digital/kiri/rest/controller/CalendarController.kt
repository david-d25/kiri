package space.davids_digital.kiri.rest.controller

import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import space.davids_digital.kiri.model.CalendarEvent
import space.davids_digital.kiri.orm.service.CalendarOrmService
import space.davids_digital.kiri.rest.dto.calendar.CalendarEventCreateRequest
import space.davids_digital.kiri.rest.dto.calendar.CalendarEventDto
import space.davids_digital.kiri.rest.dto.calendar.CalendarEventUpdateRequest
import space.davids_digital.kiri.rest.dto.calendar.CalendarOccurrenceDto
import space.davids_digital.kiri.rest.dto.calendar.CalendarRruleValidateRequest
import space.davids_digital.kiri.rest.dto.calendar.CalendarRruleValidateResponse
import space.davids_digital.kiri.service.RruleService
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@RestController
@RequestMapping("/calendar")
class CalendarController(
    private val ormService: CalendarOrmService,
    private val rruleService: RruleService,
) {
    @GetMapping("/events")
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
    ): Page<CalendarEventDto> {
        val result = if (!search.isNullOrBlank()) {
            ormService.searchByTitle(search, page, size)
        } else {
            ormService.list(page, size)
        }
        return result.map(::toDto)
    }

    @GetMapping("/events/{id}")
    fun get(@PathVariable id: UUID): CalendarEventDto {
        val event = ormService.get(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar event not found: $id")
        return toDto(event)
    }

    @PostMapping("/events")
    fun create(@RequestBody request: CalendarEventCreateRequest): CalendarEventDto {
        if (request.title.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank")
        }
        val now = ZonedDateTime.now()
        val isRecurring = !request.rrule.isNullOrBlank()
        if (isRecurring) {
            val rrule = request.rrule
            val dtstart = request.dtstart
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "dtstart is required for recurring events")
            rruleService.validate(rrule)?.let {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RRULE: $it")
            }
            val zone = request.timezone?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: dtstart.zone
            val next = rruleService.nextOccurrence(rrule, dtstart, zone, now)
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "RRULE has no future occurrences")
            val policy = parsePolicy(request.missedPolicy)
            val event = CalendarEvent(
                id = UUID.randomUUID(),
                title = request.title.trim(),
                description = request.description?.trim()?.ifBlank { null },
                timezone = zone.id,
                firesAt = null,
                rrule = rrule,
                dtstart = dtstart,
                exdates = emptyList(),
                nextFireAt = next,
                lastFiredAt = null,
                wakeAgent = request.wakeAgent,
                missedPolicy = policy,
                enabled = true,
                createdAt = now,
                updatedAt = now,
            )
            return toDto(ormService.create(event))
        } else {
            val firesAt = request.firesAt
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "firesAt is required for one-time events")
            if (firesAt.isBefore(now)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "firesAt is in the past")
            }
            val zone = request.timezone?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: firesAt.zone
            val event = CalendarEvent(
                id = UUID.randomUUID(),
                title = request.title.trim(),
                description = request.description?.trim()?.ifBlank { null },
                timezone = zone.id,
                firesAt = firesAt,
                rrule = null,
                dtstart = null,
                exdates = emptyList(),
                nextFireAt = firesAt,
                lastFiredAt = null,
                wakeAgent = request.wakeAgent,
                missedPolicy = CalendarEvent.MissedPolicy.FIRE_ONCE,
                enabled = true,
                createdAt = now,
                updatedAt = now,
            )
            return toDto(ormService.create(event))
        }
    }

    @PutMapping("/events/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: CalendarEventUpdateRequest): CalendarEventDto {
        val existing = ormService.get(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar event not found: $id")
        if (request.title.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank")
        }
        val isRecurring = !request.rrule.isNullOrBlank()
        if (isRecurring) {
            rruleService.validate(request.rrule)?.let {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RRULE: $it")
            }
            if (request.dtstart == null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "dtstart is required for recurring events")
            }
        } else if (request.firesAt == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "firesAt is required for one-time events")
        }
        val policy = parsePolicy(request.missedPolicy)
        val now = ZonedDateTime.now()
        val zone = runCatching { ZoneId.of(request.timezone) }.getOrNull()?.id ?: existing.timezone

        val nextFireAt = if (isRecurring) {
            rruleService.nextOccurrence(request.rrule, request.dtstart!!, ZoneId.of(zone), now, request.exdates)
        } else {
            request.firesAt!!.takeIf { it.isAfter(now) }
        }

        val updated = existing.copy(
            title = request.title.trim(),
            description = request.description?.trim()?.ifBlank { null },
            timezone = zone,
            firesAt = if (isRecurring) null else request.firesAt,
            rrule = request.rrule,
            dtstart = if (isRecurring) request.dtstart else null,
            exdates = request.exdates,
            nextFireAt = nextFireAt,
            wakeAgent = request.wakeAgent,
            missedPolicy = policy,
            enabled = request.enabled && (nextFireAt != null),
            updatedAt = now,
        )
        return toDto(ormService.update(updated))
    }

    @DeleteMapping("/events/{id}")
    fun delete(@PathVariable id: UUID) {
        ormService.delete(id)
    }

    @GetMapping("/events/{id}/occurrences")
    fun occurrences(
        @PathVariable id: UUID,
        @RequestParam from: ZonedDateTime,
        @RequestParam to: ZonedDateTime,
    ): List<CalendarOccurrenceDto> {
        val event = ormService.get(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar event not found: $id")
        return materialise(event, from, to)
    }

    @GetMapping("/occurrences")
    fun occurrencesInRange(
        @RequestParam from: ZonedDateTime,
        @RequestParam to: ZonedDateTime,
    ): List<CalendarOccurrenceDto> {
        if (to.isBefore(from)) return emptyList()
        val all = mutableListOf<CalendarEvent>()
        var page = 0
        while (true) {
            val p = ormService.list(page, 200)
            all.addAll(p.content)
            if (!p.hasNext()) break
            page++
        }
        return all.asSequence()
            .filter { it.enabled }
            .flatMap { materialise(it, from, to).asSequence() }
            .sortedBy { it.at }
            .toList()
    }

    @PostMapping("/validate-rrule")
    fun validateRrule(@RequestBody request: CalendarRruleValidateRequest): CalendarRruleValidateResponse {
        val error = rruleService.validate(request.rrule)
        return CalendarRruleValidateResponse(valid = error == null, error = error)
    }

    private fun materialise(event: CalendarEvent, from: ZonedDateTime, to: ZonedDateTime): List<CalendarOccurrenceDto> {
        if (event.isRecurring) {
            val rrule = event.rrule ?: return emptyList()
            val dtstart = event.dtstart ?: return emptyList()
            val zone = runCatching { ZoneId.of(event.timezone) }.getOrDefault(ZoneId.systemDefault())
            return rruleService.occurrencesInRange(rrule, dtstart, zone, from, to, event.exdates)
                .map { CalendarOccurrenceDto(event.id, event.title, it, true) }
        }
        val firesAt = event.firesAt ?: return emptyList()
        return if (!firesAt.isBefore(from) && !firesAt.isAfter(to)) {
            listOf(CalendarOccurrenceDto(event.id, event.title, firesAt, false))
        } else emptyList()
    }

    private fun parsePolicy(name: String): CalendarEvent.MissedPolicy =
        runCatching { CalendarEvent.MissedPolicy.valueOf(name.uppercase()) }
            .getOrElse { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown missedPolicy '$name'") }

    private fun toDto(event: CalendarEvent) = CalendarEventDto(
        id = event.id,
        title = event.title,
        description = event.description,
        timezone = event.timezone,
        firesAt = event.firesAt,
        rrule = event.rrule,
        dtstart = event.dtstart,
        exdates = event.exdates,
        nextFireAt = event.nextFireAt,
        lastFiredAt = event.lastFiredAt,
        wakeAgent = event.wakeAgent,
        missedPolicy = event.missedPolicy.name,
        enabled = event.enabled,
        recurring = event.isRecurring,
        createdAt = event.createdAt,
        updatedAt = event.updatedAt,
    )
}
