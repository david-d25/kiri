package space.davids_digital.kiri.rest.controller

import kotlinx.coroutines.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import space.davids_digital.kiri.agent.engine.AgentEngine
import space.davids_digital.kiri.agent.engine.EngineEventBus
import space.davids_digital.kiri.agent.engine.event.TickEvent
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.tool.AgentToolParameterMapper
import space.davids_digital.kiri.rest.SseClients
import space.davids_digital.kiri.rest.dto.*
import space.davids_digital.kiri.rest.mapper.EngineStateDtoMapper
import space.davids_digital.kiri.rest.mapper.FrameDtoMapper
import space.davids_digital.kiri.rest.mapper.ToolDtoMapper
import space.davids_digital.kiri.rest.sse.SseEventName
import space.davids_digital.kiri.rest.sse.sendEvent
import space.davids_digital.kiri.service.AdminToolService

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
    private val adminToolService: AdminToolService,
    private val toolDtoMapper: ToolDtoMapper,
    private val toolParameterMapper: AgentToolParameterMapper,
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
        val frames = snap.frames.map { frameDtoMapper.map(it) }.toMutableList()
        return FrameBufferStateDto(frames, frameBuffer.hardLimit)
    }

    @GetMapping("/tools")
    fun getTools(): List<ToolDto> = adminToolService.listTools().map { entry ->
        ToolDto(
            fullName = entry.fullName,
            description = entry.description,
            parameters = toolDtoMapper.mapParameterValue(
                toolParameterMapper.map(entry.callable)
            )
        )
    }

    @PostMapping("/tools/execute")
    suspend fun executeTool(@RequestBody request: ToolExecuteRequest): ToolCallFrameDto {
        val input = toolDtoMapper.mapToolInput(request.input)
        try {
            val frame = adminToolService.executeTool(request.toolName, input)
            return frameDtoMapper.mapToolCallFrame(frame)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        }
    }

    @GetMapping("/events/subscribe", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun eventStream(): SseEmitter {
        val emitter = sseClients.register(SseEmitter(30_000L))
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val jobs = mutableListOf<Job>()

        fun cancelJobs() {
            jobs.forEach { it.cancel() }
        }

        try {
            emitter.send(SseEmitter.event().reconnectTime(6000))
            // Startup
            emitter.sendEvent(SseEventName.FrameBufferState, getFrameBufferState())
            emitter.sendEvent(SseEventName.EngineState, engineStateMapper.toDto(engine.state.value))
        } catch (e: Exception) {
            cancelJobs()
            emitter.completeWithError(e)
            return emitter
        }

        jobs += scope.launch {
            engine.state.collect { state ->
                try {
                    emitter.sendEvent(SseEventName.EngineState, engineStateMapper.toDto(state))
                } catch (e: Exception) {
                    cancel("SSE send failed", e)
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
                }
            }
        }

        jobs += scope.launch {
            frameBuffer.updates.collect {
                try {
                    emitter.sendEvent(SseEventName.FrameBufferState, getFrameBufferState())
                } catch (e: Exception) {
                    cancel("SSE send failed", e)
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
                }
            }
        }

        emitter.onTimeout { cancelJobs() }
        emitter.onCompletion { cancelJobs() }
        return emitter
    }
}
