package space.davids_digital.kiri.rest.controller

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import space.davids_digital.kiri.agent.engine.AgentEngine
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.rest.dto.FrameDto
import space.davids_digital.kiri.rest.mapper.FrameDtoMapper
import space.davids_digital.kiri.rest.service.AdminEventEmitterService

/**
 * Administrative endpoints for monitoring and controlling the single agent instance.
 */
@RestController
@RequestMapping("/admin/agent")
class AdminAgentController(
    private val engine: AgentEngine,
    private val frameBuffer: FrameBuffer,
    private val frameDtoMapper: FrameDtoMapper,
    private val eventEmitter: AdminEventEmitterService,
) {
    @PostMapping("/start")
    @PreAuthorize("hasRole('admin')")
    fun startAgent() {
        engine.start()
    }

    @PostMapping("/stop")
    @PreAuthorize("hasRole('admin')")
    fun stopAgent() {
        engine.softStop()
    }

    @GetMapping("/buffer")
    @PreAuthorize("hasRole('admin')")
    fun snapshot(): List<FrameDto> = frameBuffer.map { frameDtoMapper.map(it) }

    @GetMapping("/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasRole('admin')")
    fun eventStream(): SseEmitter {
        val emitter = SseEmitter(0)

        // Register for future events.
        eventEmitter.register(emitter)

        emitter.onCompletion { eventEmitter.unregister(emitter) }
        emitter.onTimeout {
            eventEmitter.unregister(emitter)
            emitter.complete()
        }
        emitter.onError { _ -> eventEmitter.unregister(emitter) }

        // Optionally could send a hello event.
        return emitter
    }
}
