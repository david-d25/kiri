package space.davids_digital.kiri.model

import java.time.ZonedDateTime
import java.util.UUID

data class CalendarEvent(
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
    val missedPolicy: MissedPolicy,
    val enabled: Boolean,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
) {
    val isRecurring: Boolean get() = rrule != null

    enum class MissedPolicy {
        FIRE_ONCE, SKIP
    }
}
