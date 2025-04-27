package space.davids_digital.kiri.rest.service

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import space.davids_digital.kiri.rest.dto.AdminEventDto
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Fan‑out hub that streams lightweight admin events to all connected SSE clients.
 */
@Component
class AdminEventEmitterService {
    private val emitters = ConcurrentLinkedQueue<SseEmitter>()

    fun register(emitter: SseEmitter) {
        emitters.add(emitter)
    }

    fun unregister(emitter: SseEmitter) {
        emitters.remove(emitter)
    }

    fun push(event: AdminEventDto) {
        val iterator = emitters.iterator()
        while (iterator.hasNext()) {
            val emitter = iterator.next()
            try {
                emitter.send(event, MediaType.APPLICATION_JSON)
            } catch (e: IOException) {
                emitter.completeWithError(e)
                iterator.remove()
            }
        }
    }
}
