package space.davids_digital.kiri.rest.controller

import kotlinx.coroutines.*
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import space.davids_digital.kiri.agent.engine.AgentEngine
import space.davids_digital.kiri.agent.engine.EngineEventBus
import space.davids_digital.kiri.agent.engine.TickEvent
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.rest.SseClients
import space.davids_digital.kiri.rest.dto.FrameBufferStateDto
import space.davids_digital.kiri.rest.mapper.EngineStateDtoMapper
import space.davids_digital.kiri.rest.mapper.FrameDtoMapper
import space.davids_digital.kiri.rest.sse.SseEventName
import space.davids_digital.kiri.rest.sse.sendEvent

/**
 * Administrative endpoints for monitoring and controlling the single agent instance.
 */
@RestController
@RequestMapping("/agent")
class AgentController(
    private val engine: AgentEngine,
    private val frameBuffer: FrameBuffer,
    private val frameDtoMapper: FrameDtoMapper,
    private val engineStateMapper: EngineStateDtoMapper,
    private val sseClients: SseClients,
    private val engineEventBus: EngineEventBus,
) {
    @PostMapping("start")
    suspend fun start() {
        engine.start()
    }

    @PostMapping("stop")
    suspend fun stop() {
        engine.softStop()
    }

    @PostMapping("tick")
    suspend fun tick() {
        engine.tick()
    }

    @PostMapping("hard-stop")
    suspend fun hardStop() {
        engine.hardStop()
    }

    @GetMapping("/framebuffer")
    suspend fun getFrameBufferState(): FrameBufferStateDto {
        val snap = frameBuffer.snapshot()
        val fixedFrames = snap.fixed.map { frameDtoMapper.mapDataFrame(it) }.toMutableList()
        val rollingFrames = snap.rolling.map { frameDtoMapper.map(it) }.toMutableList()
        return FrameBufferStateDto(fixedFrames, rollingFrames, snap.hardLimit)
    }

    @GetMapping("/events/subscribe", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun eventStream(): SseEmitter {
        val emitter = sseClients.register(SseEmitter(30_000L))
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val jobs = mutableListOf<Job>()

        fun complete(e: Exception? = null) {
            jobs.forEach { it.cancel() }
            if (e != null) {
                emitter.completeWithError(e)
            } else {
                emitter.complete()
            }
        }

        try {
            emitter.send(SseEmitter.event().reconnectTime(6000))
            // Startup
            emitter.sendEvent(SseEventName.FrameBufferState, getFrameBufferState())
            emitter.sendEvent(SseEventName.EngineState, engineStateMapper.toDto(engine.state.value))
        } catch (e: Exception) {
            complete(e)
            return emitter
        }

        jobs += scope.launch {
            engine.state.collect { state ->
                try {
                    emitter.sendEvent(SseEventName.EngineState, engineStateMapper.toDto(state))
                } catch (e: Exception) {
                    cancel("SSE send failed", e)
                    complete(e)
                }
            }
        }

        jobs += scope.launch {
            engineEventBus.events.collect { event ->
                try {
                    when (event) {
                        is TickEvent -> emitter.sendEvent(SseEventName.FrameBufferState, getFrameBufferState())
                    }
                } catch (e: Exception) {
                    cancel("SSE send failed", e)
                    complete(e)
                }
            }
        }

        jobs += scope.launch {
            frameBuffer.updates.collect {
                try {
                    emitter.sendEvent(SseEventName.FrameBufferState, getFrameBufferState())
                } catch (e: Exception) {
                    cancel("SSE send failed", e)
                    complete(e)
                }
            }
        }

        jobs += scope.launch {
            while (isActive) {
                delay(20_000)
                try {
                    emitter.sendEvent(SseEventName.Heartbeat, Unit)
                } catch (e: Exception) {
                    cancel("Heartbeat failed", e)
                    complete(e)
                }
            }
        }

        emitter.onTimeout { complete() }
        emitter.onCompletion { complete() }
        return emitter
    }
}
