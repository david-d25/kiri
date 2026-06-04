package space.davids_digital.kiri.agent.notification

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.engine.EngineEventBus
import space.davids_digital.kiri.agent.engine.event.WakeUpRequestEvent
import space.davids_digital.kiri.agent.frame.DataFrameUtils.asPrettyString
import space.davids_digital.kiri.agent.frame.FrameBuffer

@Service
class NotificationManager(
    private val frames: FrameBuffer,
    private val eventBus: EngineEventBus,
) {
    /**
     * Push a notification into the agent's frame buffer.
     *
     * @param wake when true (default), also emit a wake-up event so a sleeping agent resumes.
     *             Pass false for ambient calendar events that should appear in the buffer
     *             but not interrupt sleep — the agent will see them on its next natural wake.
     */
    fun push(notification: Notification, wake: Boolean = true) {
        frames.addStatic {
            tag = "notification"
            content = notification.content
            attributes["sent-at"] = notification.sentAt.asPrettyString()
            attributes.putAll(notification.metadata)
        }
        if (wake) {
            runBlocking {
                eventBus.events.emit(WakeUpRequestEvent())
            }
        }
    }
}