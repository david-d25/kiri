package space.davids_digital.kiri.service

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.notification.Notification
import space.davids_digital.kiri.agent.notification.NotificationManager
import space.davids_digital.kiri.model.CalendarEvent
import space.davids_digital.kiri.orm.service.CalendarOrmService
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

/**
 * Calendar domain logic: occurrence resolution, firing, polling, startup catch-up.
 *
 * Polling cadence is fixed at 10 seconds — short enough to feel responsive for reminders,
 * relaxed enough to keep the DB load negligible. Accuracy is ±10s.
 */
@Service
class CalendarService(
    private val ormService: CalendarOrmService,
    private val rruleService: RruleService,
    private val notificationManager: NotificationManager,
) {
    companion object {
        private const val POLL_INTERVAL_MS = 10_000L
        private const val POLL_BATCH_LIMIT = 50
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        try {
            catchUpMissed()
        } catch (e: Exception) {
            log.error("Calendar startup catch-up failed", e)
        }
    }

    @Scheduled(fixedDelay = POLL_INTERVAL_MS)
    fun pollTick() {
        try {
            fireDue()
        } catch (e: Exception) {
            log.error("Calendar poll tick failed", e)
        }
    }

    /**
     * Compute the next fire timestamp for an event from a given reference point.
     * - One-shot events: returns [CalendarEvent.firesAt] if it is strictly after [from], else null.
     * - Recurring events: walks the RRULE from [CalendarEvent.dtstart], skipping any timestamps
     *   listed in [CalendarEvent.exdates] and returning the first strictly after [from].
     */
    fun computeNextFireAt(event: CalendarEvent, from: ZonedDateTime): ZonedDateTime? {
        return if (event.isRecurring) {
            val dtstart = event.dtstart ?: return null
            val rrule = event.rrule ?: return null
            val zone = runCatching { ZoneId.of(event.timezone) }.getOrDefault(ZoneId.of("UTC"))
            val anchor = if (dtstart.isAfter(from)) dtstart.minusNanos(1) else from
            rruleService.nextOccurrence(rrule, dtstart, zone, anchor, event.exdates)
        } else {
            event.firesAt?.takeIf { it.isAfter(from) }
        }
    }

    /**
     * Catch up on events whose next_fire_at slipped into the past while the service was down.
     * For each missed event:
     *   - FIRE_ONCE: fire once, then advance next_fire_at (recurring) or disable (one-shot).
     *   - SKIP: advance next_fire_at without firing (recurring); disable if one-shot.
     */
    private fun catchUpMissed() {
        val now = ZonedDateTime.now()
        val due = ormService.findDue(now, POLL_BATCH_LIMIT)
        if (due.isEmpty()) return
        log.info("Catching up on ${due.size} missed calendar event(s)")
        for (event in due) {
            handleDue(event, now, isCatchUp = true)
        }
    }

    private fun fireDue() {
        val now = ZonedDateTime.now()
        val due = ormService.findDue(now, POLL_BATCH_LIMIT)
        for (event in due) {
            handleDue(event, now, isCatchUp = false)
        }
    }

    private fun handleDue(event: CalendarEvent, now: ZonedDateTime, isCatchUp: Boolean) {
        try {
            val shouldFire = when (event.missedPolicy) {
                CalendarEvent.MissedPolicy.FIRE_ONCE -> true
                CalendarEvent.MissedPolicy.SKIP -> !isCatchUp
            }
            if (shouldFire) {
                fire(event, now)
            }
            advance(event, now)
        } catch (e: Exception) {
            log.error("Failed to handle due event ${event.id}", e)
        }
    }

    private fun fire(event: CalendarEvent, firedAt: ZonedDateTime) {
        val nextAfter = computeNextFireAt(event, firedAt)
        notificationManager.push(
            notification = Notification(
                sentAt = firedAt,
                metadata = mapOf(
                    "app" to "calendar",
                    "eventId" to event.id.shortHex(),
                ),
                content = dataFrameContent {
                    text(renderEventBody(event, nextAfter))
                }
            ),
            wake = event.wakeAgent,
        )
        log.info(
            "Fired calendar event {} '{}' (wake={}, recurring={})",
            event.id, event.title, event.wakeAgent, event.isRecurring
        )
    }

    private fun advance(event: CalendarEvent, now: ZonedDateTime) {
        if (event.isRecurring) {
            val next = computeNextFireAt(event, now)
            ormService.update(
                event.copy(
                    nextFireAt = next,
                    lastFiredAt = now,
                    enabled = next != null,
                )
            )
        } else {
            ormService.update(
                event.copy(
                    nextFireAt = null,
                    lastFiredAt = now,
                    enabled = false,
                )
            )
        }
    }

    private fun renderEventBody(
        event: CalendarEvent,
        nextOccurrence: ZonedDateTime?,
    ): String = buildString {
        appendLine("Calendar reminder: ${event.title}")
        event.description?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            appendLine(it)
        }
        if (event.isRecurring && nextOccurrence != null) {
            appendLine()
            appendLine("Next occurrence: ${formatWithWeekday(nextOccurrence)}")
        }
    }.trimEnd()

    private fun UUID.shortHex(): String = toString().replace("-", "").take(8)

    private fun formatWithWeekday(time: ZonedDateTime): String {
        val weekday = time.dayOfWeek.toShort()
        return "$weekday ${time.year}-${time.monthValue.pad()}-${time.dayOfMonth.pad()} " +
                "${time.hour.pad()}:${time.minute.pad()} ${time.offset}"
    }

    private fun DayOfWeek.toShort(): String =
        getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

    private fun Int.pad(): String = toString().padStart(2, '0')
}
