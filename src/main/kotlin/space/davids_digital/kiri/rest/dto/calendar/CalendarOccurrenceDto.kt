package space.davids_digital.kiri.rest.dto.calendar

import java.time.ZonedDateTime
import java.util.UUID

data class CalendarOccurrenceDto(
    val eventId: UUID,
    val title: String,
    val at: ZonedDateTime,
    val recurring: Boolean,
)
