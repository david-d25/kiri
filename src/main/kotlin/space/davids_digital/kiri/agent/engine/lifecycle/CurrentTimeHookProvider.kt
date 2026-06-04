package space.davids_digital.kiri.agent.engine.lifecycle

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Injects a `<current_time>` static frame at the start of every wake so the agent has
 * an authoritative anchor for weekday, week-of-year, day-of-year, and timezone offset
 * without having to compute them from raw ISO strings.
 */
@Component
class CurrentTimeHookProvider : LifecycleHookProvider {

    @OnAgentWake
    fun pushCurrentTime(frames: FrameBuffer) {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val weekFields = WeekFields.of(Locale.ENGLISH)
        val weekOfYear = now.get(weekFields.weekOfWeekBasedYear())
        val dayOfYear = now.dayOfYear
        val daysInYear = if (now.toLocalDate().isLeapYear) 366 else 365
        val weekdayLong = now.dayOfWeek.fullName()
        val weekdayShort = now.dayOfWeek.shortName()
        val monthLong = now.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

        val human = buildString {
            append("$weekdayLong, ${now.dayOfMonth} $monthLong ${now.year}, ")
            append("${now.hour.pad()}:${now.minute.pad()} (${now.zone}). ")
            append("Week $weekOfYear. Day $dayOfYear of $daysInYear.")
        }

        frames.addStatic {
            tag = "current_time"
            attributes["iso"] = now.toOffsetDateTime().toString()
            attributes["weekday"] = weekdayShort
            attributes["week-of-year"] = weekOfYear.toString()
            attributes["day-of-year"] = dayOfYear.toString()
            attributes["zone"] = now.zone.id
            content = dataFrameContent { text(human) }
        }
    }

    private fun DayOfWeek.fullName() = getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    private fun DayOfWeek.shortName() = getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    private fun Int.pad() = toString().padStart(2, '0')
}
