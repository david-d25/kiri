package space.davids_digital.kiri.rest.dto.calendar

import java.time.ZonedDateTime

data class CalendarEventCreateRequest(
    val title: String,
    val description: String? = null,
    val timezone: String? = null,
    val firesAt: ZonedDateTime? = null,
    val rrule: String? = null,
    val dtstart: ZonedDateTime? = null,
    val wakeAgent: Boolean = true,
    val missedPolicy: String = "FIRE_ONCE",
)
