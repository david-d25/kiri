package space.davids_digital.kiri.service

import net.fortuna.ical4j.model.Recur
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Thin wrapper over ical4j RFC 5545 RRULE handling.
 * All computations are performed in the event's timezone — RRULE semantics like
 * "every Monday at 09:00" depend on local time, not UTC.
 */
@Service
class RruleService {

    companion object {
        /** Cap on how far into the future to project recurrences. */
        private const val PROJECTION_YEARS = 10L

        /** Safety cap on materialised occurrences inside a window. */
        private const val MAX_OCCURRENCES_PER_QUERY = 1000

        private val UNTIL_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
    }

    /** Returns an error message if [rrule] is invalid, or null if it parses cleanly. */
    fun validate(rrule: String): String? {
        return try {
            Recur<LocalDateTime>(rrule)
            null
        } catch (e: Exception) {
            e.message ?: e::class.simpleName ?: "Invalid RRULE"
        }
    }

    /**
     * Next occurrence of [rrule] anchored at [dtstart] that is strictly after [after],
     * excluding any timestamps in [exdates]. Returns null if the rule terminates first.
     */
    fun nextOccurrence(
        rrule: String,
        dtstart: ZonedDateTime,
        timezone: ZoneId,
        after: ZonedDateTime,
        exdates: List<ZonedDateTime> = emptyList(),
    ): ZonedDateTime? {
        val window = occurrencesInRange(
            rrule = rrule,
            dtstart = dtstart,
            timezone = timezone,
            from = after,
            to = after.plusYears(PROJECTION_YEARS),
            exdates = exdates,
            inclusiveFrom = false,
        )
        return window.firstOrNull()
    }

    /**
     * Materialise occurrences within [from, to] (or (from, to] if [inclusiveFrom] is false).
     * Limited to [MAX_OCCURRENCES_PER_QUERY] to bound runtime; callers should choose a sane window.
     */
    fun occurrencesInRange(
        rrule: String,
        dtstart: ZonedDateTime,
        timezone: ZoneId,
        from: ZonedDateTime,
        to: ZonedDateTime,
        exdates: List<ZonedDateTime> = emptyList(),
        inclusiveFrom: Boolean = true,
    ): List<ZonedDateTime> {
        if (to.isBefore(from)) return emptyList()
        val recur = Recur<LocalDateTime>(rrule)
        val seed = dtstart.withZoneSameInstant(timezone).toLocalDateTime()
        val periodStart = from.withZoneSameInstant(timezone).toLocalDateTime()
        val periodEnd = to.withZoneSameInstant(timezone).toLocalDateTime()
        val exLocal = exdates.map { it.withZoneSameInstant(timezone).toLocalDateTime() }.toSet()

        val raw = recur.getDates(seed, periodStart, periodEnd, MAX_OCCURRENCES_PER_QUERY)
        return raw.asSequence()
            .filter { if (inclusiveFrom) !it.isBefore(periodStart) else it.isAfter(periodStart) }
            .filter { !it.isAfter(periodEnd) }
            .filter { it !in exLocal }
            .map { it.atZone(timezone) }
            .toList()
    }

    /**
     * Return a copy of [rrule] with its termination set to [until] (inclusive — RRULE UNTIL
     * semantics: the last occurrence is on or before this instant). Replaces any existing
     * UNTIL or COUNT parameter, because RFC 5545 forbids both in the same rule.
     *
     * UNTIL is serialised in UTC basic ISO format (e.g. `20260804T120000Z`) per the spec.
     */
    fun withUntil(rrule: String, until: ZonedDateTime): String {
        val utc = until.withZoneSameInstant(ZoneOffset.UTC)
        val formatted = utc.format(UNTIL_FORMATTER)
        val parts = rrule
            .split(";")
            .filter { it.isNotBlank() }
            .filterNot { it.uppercase().startsWith("UNTIL=") || it.uppercase().startsWith("COUNT=") }
            .toMutableList()
        parts.add("UNTIL=$formatted")
        return parts.joinToString(";")
    }

}
