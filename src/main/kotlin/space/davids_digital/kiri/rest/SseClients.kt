package space.davids_digital.kiri.rest

import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Component
class SseClients {
    private val clients = ConcurrentHashMap.newKeySet<SseEmitter>()

    fun register(emitter: SseEmitter): SseEmitter {
        clients += emitter
        emitter.onCompletion { clients -= emitter }
        emitter.onTimeout { clients -= emitter }
        return emitter
    }

    @EventListener(ContextClosedEvent::class)
    fun closeAll() {
        clients.forEach { runCatching { it.complete() } }
        clients.clear()
    }
}