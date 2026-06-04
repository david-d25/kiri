package space.davids_digital.kiri.rest.dto.calendar

import java.time.ZonedDateTime
import java.util.UUID

data class CalendarEventDto(
    val id: UUID,
    val title: String,
    val description: String?,
    val timezone: String,
    val firesAt: ZonedDateTime?,
    val rrule: String?,
    val dtstart: ZonedDateTime?,
    val exdates: List<ZonedDateTime>,
    val nextFireAt: ZonedDateTime?,
    val lastFiredAt: ZonedDateTime?,
    val wakeAgent: Boolean,
    val missedPolicy: String,
    val enabled: Boolean,
    val recurring: Boolean,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
)
