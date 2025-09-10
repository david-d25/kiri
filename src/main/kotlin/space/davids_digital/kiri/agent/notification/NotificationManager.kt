package space.davids_digital.kiri.agent.notification

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.engine.EngineEvent
import space.davids_digital.kiri.agent.engine.EngineEventBus
import space.davids_digital.kiri.agent.engine.WakeUpRequestEvent
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.frame.asPrettyString

@Service
class NotificationManager(
    private val frames: FrameBuffer,
    private val eventBus: EngineEventBus,
) {
    fun push(notification: Notification) {
        frames.addStatic {
            tag = "notification"
            content = notification.content
            attributes["sent-at"] = notification.sentAt.asPrettyString()
            attributes.putAll(notification.metadata)
        }
        runBlocking {
            eventBus.events.emit(WakeUpRequestEvent()) // TODO
        }
    }
}