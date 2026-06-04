package space.davids_digital.kiri.agent.app.calendar

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.model.CalendarEvent
import space.davids_digital.kiri.orm.service.CalendarOrmService
import space.davids_digital.kiri.service.RruleService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.reflect.KFunction

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@AgentToolNamespace("calendar")
class CalendarApp(
    private val ormService: CalendarOrmService,
    private val rruleService: RruleService,
) : AgentApp("calendar") {

    companion object {
        private const val DEFAULT_LIST_DAYS = 14
        private const val MAX_LIST_OCCURRENCES = 200
        private const val MAX_OCCURRENCES_PER_RECURRING = 30
        private const val MAX_TITLE_LENGTH = 256

        private val FRIENDLY_FORMATS = listOf(
            "yyyy-MM-dd HH:mm:ss XXX",
            "yyyy-MM-dd HH:mm XXX",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
        )
    }

    @AgentToolMethod(
        description = "Schedule a one-time reminder. Accepts ISO (2026-06-15T14:30:00+03:00) or " +
                "'2026-06-15 14:30' (system tz)."
    )
    suspend fun createOneTimeReminder(
        title: String,
        @AgentToolParameter(description = "ISO or 'YYYY-MM-DD HH:mm'")
        firesAt: String,
        description: String? = null,
        @AgentToolParameter(description = "If true, wake the agent on fire")
        wakeAgent: Boolean = true,
    ): String {
        val parsed = parseFlexibleTime(firesAt) ?: return "Could not parse firesAt='$firesAt'."
        if (title.isBlank()) return "Title must not be blank."
        if (title.length > MAX_TITLE_LENGTH) return "Title too long (max $MAX_TITLE_LENGTH chars)."
        val now = ZonedDateTime.now()
        if (parsed.isBefore(now)) return "firesAt is in the past (${formatWithWeekday(parsed)})."
        val event = CalendarEvent(
            id = UUID.randomUUID(),
            title = title.trim(),
            description = description?.trim()?.ifBlank { null },
            timezone = parsed.zone.id,
            firesAt = parsed,
            rrule = null,
            dtstart = null,
            exdates = emptyList(),
            nextFireAt = parsed,
            lastFiredAt = null,
            wakeAgent = wakeAgent,
            missedPolicy = CalendarEvent.MissedPolicy.FIRE_ONCE,
            enabled = true,
            createdAt = now,
            updatedAt = now,
        )
        val saved = ormService.create(event)
        return "Created reminder [${saved.id.shortHex()}] '${saved.title}' for ${formatWithWeekday(parsed)}."
    }

    @AgentToolMethod(
        description = "Schedule a recurring reminder via RFC 5545 RRULE. " +
                "Examples: FREQ=DAILY;BYHOUR=9;BYMINUTE=0 / FREQ=WEEKLY;BYDAY=MO,WE,FR;BYHOUR=14 / " +
                "FREQ=MONTHLY;BYMONTHDAY=1 / FREQ=YEARLY;BYMONTH=12;BYMONTHDAY=31. " +
                "dtstart anchors the rule and provides time-of-day if BYHOUR/BYMINUTE absent."
    )
    suspend fun createRecurringReminder(
        title: String,
        rrule: String,
        @AgentToolParameter(description = "Anchor time. ISO or 'YYYY-MM-DD HH:mm'.")
        dtstart: String,
        description: String? = null,
        @AgentToolParameter(description = "If true, wake the agent on fire")
        wakeAgent: Boolean = true,
        @AgentToolParameter(description = "FIRE_ONCE collapses missed occurrences; SKIP drops them.")
        missedPolicy: String = "FIRE_ONCE",
    ): String {
        if (title.isBlank()) return "Title must not be blank."
        if (title.length > MAX_TITLE_LENGTH) return "Title too long (max $MAX_TITLE_LENGTH chars)."
        val rruleError = rruleService.validate(rrule)
        if (rruleError != null) return "Invalid RRULE: $rruleError"
        val parsedStart = parseFlexibleTime(dtstart) ?: return "Could not parse dtstart='$dtstart'."
        val policy = runCatching { CalendarEvent.MissedPolicy.valueOf(missedPolicy.uppercase()) }
            .getOrElse { return "Unknown missedPolicy '$missedPolicy'. Use FIRE_ONCE or SKIP." }

        val now = ZonedDateTime.now()
        val zone = parsedStart.zone
        val next = rruleService.nextOccurrence(rrule, parsedStart, zone, now)
            ?: return "RRULE produced no occurrences after now (${formatWithWeekday(now)})."

        val event = CalendarEvent(
            id = UUID.randomUUID(),
            title = title.trim(),
            description = description?.trim()?.ifBlank { null },
            timezone = zone.id,
            firesAt = null,
            rrule = rrule,
            dtstart = parsedStart,
            exdates = emptyList(),
            nextFireAt = next,
            lastFiredAt = null,
            wakeAgent = wakeAgent,
            missedPolicy = policy,
            enabled = true,
            createdAt = now,
            updatedAt = now,
        )
        val saved = ormService.create(event)
        return "Created recurring reminder [${saved.id.shortHex()}] '${saved.title}'. " +
                "Next occurrence: ${formatWithWeekday(next)}."
    }

    @AgentToolMethod(description = "List upcoming events grouped by day")
    suspend fun listEvents(
        @AgentToolParameter(description = "Range start (inclusive). Default: now.")
        from: String? = null,
        @AgentToolParameter(description = "Range end (inclusive). Default: now + $DEFAULT_LIST_DAYS days.")
        to: String? = null,
        @AgentToolParameter(description = "Case-insensitive title substring filter.")
        query: String? = null,
        @AgentToolParameter(description = "Include disabled events.")
        includeDisabled: Boolean = false,
    ): String {
        val now = ZonedDateTime.now()
        val fromTime = from?.let { parseFlexibleTime(it) ?: return "Could not parse from='$it'." } ?: now
        val toTime = to?.let { parseFlexibleTime(it) ?: return "Could not parse to='$it'." }
            ?: fromTime.plusDays(DEFAULT_LIST_DAYS.toLong())
        if (toTime.isBefore(fromTime)) return "'to' is before 'from'."

        val all = mutableListOf<CalendarEvent>()
        var page = 0
        while (true) {
            val pageResult = ormService.list(page, 200)
            all.addAll(pageResult.content)
            if (!pageResult.hasNext()) break
            page++
        }

        val filtered = all.asSequence()
            .filter { includeDisabled || it.enabled }
            .filter { query.isNullOrBlank() || it.title.contains(query, ignoreCase = true) }
            .toList()

        data class Occurrence(val at: ZonedDateTime, val event: CalendarEvent)

        val occurrences = mutableListOf<Occurrence>()
        for (event in filtered) {
            if (event.isRecurring) {
                val rrule = event.rrule ?: continue
                val dtstart = event.dtstart ?: continue
                val zone = runCatching { ZoneId.of(event.timezone) }.getOrDefault(ZoneId.systemDefault())
                val window = rruleService.occurrencesInRange(rrule, dtstart, zone, fromTime, toTime, event.exdates)
                window.take(MAX_OCCURRENCES_PER_RECURRING).forEach { occurrences += Occurrence(it, event) }
            } else {
                val at = event.firesAt ?: continue
                if (!at.isBefore(fromTime) && !at.isAfter(toTime)) {
                    occurrences += Occurrence(at, event)
                }
            }
        }

        if (occurrences.isEmpty()) {
            return "No events between ${formatWithWeekday(fromTime)} and ${formatWithWeekday(toTime)}."
        }

        val sorted = occurrences.sortedBy { it.at }.take(MAX_LIST_OCCURRENCES)
        val truncated = occurrences.size > MAX_LIST_OCCURRENCES
        val today = now.toLocalDate()

        return buildString {
            appendLine("Events from ${formatWithWeekday(fromTime)} to ${formatWithWeekday(toTime)}:")
            appendLine()
            var lastDate: LocalDate? = null
            for (occ in sorted) {
                val date = occ.at.toLocalDate()
                if (date != lastDate) {
                    if (lastDate != null) appendLine()
                    appendLine(dayHeader(date, today))
                    lastDate = date
                }
                append("  ${occ.at.hour.pad()}:${occ.at.minute.pad()} — ${occ.event.title}")
                append(" [${occ.event.id.shortHex()}]")
                if (occ.event.isRecurring) append(" (recurring)")
                if (!occ.event.enabled) append(" (disabled)")
                appendLine()
            }
            if (truncated) {
                appendLine()
                appendLine("…${occurrences.size - MAX_LIST_OCCURRENCES} more occurrences truncated. Narrow the range.")
            }
        }.trimEnd()
    }

    @AgentToolMethod(description = "Show full details of a single event by id or hex prefix.")
    suspend fun getEvent(
        id: String,
    ): String {
        val event = resolveEvent(id) ?: return "No event matching '$id'."
        return renderEventDetails(event)
    }

    @AgentToolMethod(
        description = "Edit title/description/wakeAgent. To change time or recurrence, delete and recreate. " +
                "Omit a param to leave it; pass empty string to clear description."
    )
    suspend fun editEvent(
        id: String,
        title: String? = null,
        description: String? = null,
        wakeAgent: Boolean? = null,
    ): String {
        val event = resolveEvent(id) ?: return "No event matching '$id'."
        if (title != null && title.length > MAX_TITLE_LENGTH) return "Title too long (max $MAX_TITLE_LENGTH chars)."
        val updated = event.copy(
            title = title?.trim()?.ifBlank { event.title } ?: event.title,
            description = if (description == null) event.description else description.trim().ifBlank { null },
            wakeAgent = wakeAgent ?: event.wakeAgent,
        )
        ormService.update(updated)
        return "Updated [${event.id.shortHex()}] '${updated.title}'."
    }

    @AgentToolMethod(description = "Delete an event. For recurring events this removes the whole series.")
    suspend fun deleteEvent(
        id: String,
    ): String {
        val event = resolveEvent(id) ?: return "No event matching '$id'."
        ormService.delete(event.id)
        return "Deleted [${event.id.shortHex()}] '${event.title}'."
    }

    @AgentToolMethod(
        description = "Skip one occurrence of a recurring event; the series continues."
    )
    suspend fun deleteOneOccurrence(
        id: String,
        @AgentToolParameter(description = "The occurrence to skip. Must match an actual occurrence.")
        occurrenceAt: String,
    ): String {
        val event = resolveEvent(id) ?: return "No event matching '$id'."
        if (!event.isRecurring) return "Event [${event.id.shortHex()}] is not recurring."
        val parsed = parseFlexibleTime(occurrenceAt) ?: return "Could not parse occurrenceAt='$occurrenceAt'."

        val zone = runCatching { ZoneId.of(event.timezone) }.getOrDefault(ZoneId.systemDefault())
        val rrule = event.rrule ?: return "Event has no RRULE."
        val dtstart = event.dtstart ?: return "Event has no dtstart."
        val window = rruleService.occurrencesInRange(
            rrule, dtstart, zone,
            parsed.minusMinutes(1), parsed.plusMinutes(1), event.exdates,
        )
        val match = window.firstOrNull { it.toInstant() == parsed.toInstant() }
            ?: return "No occurrence at ${formatWithWeekday(parsed)} (already excluded, or doesn't match the rule)."

        val newExdates = event.exdates + match
        val nextAfter = rruleService.nextOccurrence(rrule, dtstart, zone, ZonedDateTime.now(), newExdates)
        ormService.update(
            event.copy(
                exdates = newExdates,
                nextFireAt = nextAfter,
                enabled = nextAfter != null,
            )
        )
        return "Cancelled occurrence ${formatWithWeekday(match)} of [${event.id.shortHex()}]. " +
                (if (nextAfter != null) "Next: ${formatWithWeekday(nextAfter)}." else "No more occurrences; event disabled.")
    }

    @AgentToolMethod(description = "End a recurring series at the given occurrence (it remains; later ones are dropped).")
    suspend fun stopRecurringAfter(
        id: String,
        @AgentToolParameter(description = "Last occurrence to keep.")
        lastOccurrenceAt: String,
    ): String {
        val event = resolveEvent(id) ?: return "No event matching '$id'."
        if (!event.isRecurring) return "Event [${event.id.shortHex()}] is not recurring."
        val rrule = event.rrule ?: return "Event has no RRULE."
        val dtstart = event.dtstart ?: return "Event has no dtstart."
        val parsed = parseFlexibleTime(lastOccurrenceAt) ?: return "Could not parse lastOccurrenceAt='$lastOccurrenceAt'."

        val newRrule = rruleService.withUntil(rrule, parsed)
        val zone = runCatching { ZoneId.of(event.timezone) }.getOrDefault(ZoneId.systemDefault())
        val nextAfter = rruleService.nextOccurrence(newRrule, dtstart, zone, ZonedDateTime.now(), event.exdates)
        ormService.update(
            event.copy(
                rrule = newRrule,
                nextFireAt = nextAfter,
                enabled = nextAfter != null,
            )
        )
        return "Truncated series [${event.id.shortHex()}] to end at ${formatWithWeekday(parsed)}. " +
                (if (nextAfter != null) "Next remaining: ${formatWithWeekday(nextAfter)}." else "No more occurrences; event disabled.")
    }

    @AgentToolMethod(
        description = "Delay the next fire by hours+minutes. For recurring events, only the immediate next fire is delayed."
    )
    suspend fun snooze(
        id: String,
        hours: Long = 0,
        minutes: Long = 0,
    ): String {
        val event = resolveEvent(id) ?: return "No event matching '$id'."
        val totalMinutes = hours * 60 + minutes
        if (totalMinutes <= 0) return "Snooze duration must be positive."
        val base = event.nextFireAt ?: ZonedDateTime.now()
        val newNext = base.plusMinutes(totalMinutes)

        val updated = if (event.isRecurring) {
            event.copy(nextFireAt = newNext, enabled = true)
        } else {
            event.copy(firesAt = newNext, nextFireAt = newNext, enabled = true)
        }
        ormService.update(updated)
        return "Snoozed [${event.id.shortHex()}] to ${formatWithWeekday(newNext)}."
    }

    override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> = listOf(
        ::createOneTimeReminder,
        ::createRecurringReminder,
        ::listEvents,
        ::getEvent,
        ::editEvent,
        ::deleteEvent,
        ::deleteOneOccurrence,
        ::stopRecurringAfter,
        ::snooze,
    )

    private fun resolveEvent(idOrPrefix: String): CalendarEvent? {
        val trimmed = idOrPrefix.trim().lowercase()
        if (trimmed.isBlank()) return null
        runCatching { UUID.fromString(trimmed) }.getOrNull()?.let { return ormService.get(it) }
        val candidates = ormService.findIdsByHexPrefix(trimmed)
        return when (candidates.size) {
            0 -> null
            1 -> ormService.get(candidates.first())
            else -> null
        }
    }

    private fun renderEventDetails(event: CalendarEvent): String = buildString {
        val zone = runCatching { ZoneId.of(event.timezone) }.getOrDefault(ZoneId.systemDefault())
        fun ZonedDateTime.inEventZone(): ZonedDateTime = withZoneSameInstant(zone)
        appendLine("Event [${event.id.shortHex()}] — ${event.title}")
        appendLine("Full id: ${event.id}")
        appendLine("Enabled: ${event.enabled}")
        appendLine("Wake agent: ${event.wakeAgent}")
        appendLine("Timezone: ${event.timezone}")
        if (event.isRecurring) {
            appendLine("Recurring: yes")
            appendLine("RRULE: ${event.rrule}")
            event.dtstart?.let { appendLine("dtstart: ${formatWithWeekday(it.inEventZone())}") }
            appendLine("Missed policy: ${event.missedPolicy}")
            if (event.exdates.isNotEmpty()) {
                appendLine("Cancelled occurrences:")
                event.exdates.sorted().forEach { appendLine("  - ${formatWithWeekday(it.inEventZone())}") }
            }
        } else {
            appendLine("Recurring: no")
            event.firesAt?.let { appendLine("Fires at: ${formatWithWeekday(it.inEventZone())}") }
        }
        event.nextFireAt?.let { appendLine("Next fire: ${formatWithWeekday(it.inEventZone())}") }
        event.lastFiredAt?.let { appendLine("Last fired: ${formatWithWeekday(it.inEventZone())}") }
        event.description?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            appendLine("Description:")
            appendLine(it)
        }
    }.trimEnd()

    private fun parseFlexibleTime(input: String): ZonedDateTime? {
        val raw = input.trim()
        if (raw.isEmpty()) return null

        val stripped = stripWeekdayPrefix(raw)
        val defaultZone = ZoneId.systemDefault()

        runCatching { return ZonedDateTime.parse(stripped) }
        runCatching { return java.time.OffsetDateTime.parse(stripped).toZonedDateTime() }
        runCatching { return java.time.Instant.parse(stripped).atZone(defaultZone) }
        runCatching { return LocalDateTime.parse(stripped).atZone(defaultZone) }

        for (pattern in FRIENDLY_FORMATS) {
            val formatter = DateTimeFormatter.ofPattern(pattern)
            runCatching {
                return if (pattern.contains("X")) {
                    ZonedDateTime.parse(stripped, formatter)
                } else {
                    LocalDateTime.parse(stripped, formatter).atZone(defaultZone)
                }
            }
        }
        return null
    }

    private fun stripWeekdayPrefix(input: String): String {
        val parts = input.split(" ", limit = 2)
        if (parts.size != 2) return input
        val first = parts[0].lowercase()
        val weekdayPrefixes = setOf(
            "mon", "tue", "wed", "thu", "fri", "sat", "sun",
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
        )
        return if (first.trimEnd(',') in weekdayPrefixes) parts[1] else input
    }

    private fun dayHeader(date: LocalDate, today: LocalDate): String {
        val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val prefix = when {
            date == today -> "Today, "
            date == today.plusDays(1) -> "Tomorrow, "
            else -> ""
        }
        return "$prefix$weekday, $date"
    }

    private fun formatWithWeekday(time: ZonedDateTime): String {
        val weekday = time.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        return "$weekday ${time.year}-${time.monthValue.pad()}-${time.dayOfMonth.pad()} " +
                "${time.hour.pad()}:${time.minute.pad()} ${time.offset}"
    }

    private fun UUID.shortHex(): String = toString().replace("-", "").take(8)
    private fun Int.pad(): String = toString().padStart(2, '0')
}
