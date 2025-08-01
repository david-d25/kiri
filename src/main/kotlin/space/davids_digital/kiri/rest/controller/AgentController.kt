package space.davids_digital.kiri.rest.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import space.davids_digital.kiri.agent.engine.AgentEngine
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.rest.dto.DataFrameDto
import space.davids_digital.kiri.rest.dto.FrameBufferStateDto
import space.davids_digital.kiri.rest.dto.FrameDto
import space.davids_digital.kiri.rest.mapper.FrameDtoMapper

/**
 * Administrative endpoints for monitoring and controlling the single agent instance.
 */
@RestController
@RequestMapping("/admin/agent")
class AgentController(
    private val engine: AgentEngine,
    private val frameBuffer: FrameBuffer,
    private val frameDtoMapper: FrameDtoMapper,
) {
    @PostMapping("/start")
    fun startAgent() {
        engine.start()
    }

    @PostMapping("/stop")
    fun stopAgent() {
        engine.softStop()
    }

    @GetMapping("/framebuffer")
    fun getFrameBufferState(): FrameBufferStateDto {
        val fixedFrames = mutableListOf<DataFrameDto>()
        val rollingFrames = mutableListOf<FrameDto>()
        frameBuffer.onlyFixed.forEach { frame ->
            fixedFrames.add(frameDtoMapper.mapDataFrame(frame))
        }
        frameBuffer.onlyRolling.forEach { frame ->
            rollingFrames.add(frameDtoMapper.map(frame))
        }
        return FrameBufferStateDto(fixedFrames, rollingFrames, frameBuffer.hardLimit);
    }

    @GetMapping("/events/subscribe", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun eventStream(): SseEmitter {
        val emitter = SseEmitter(0)
//        eventEmitter.register(emitter)
//        emitter.onCompletion { eventEmitter.unregister(emitter) }
//        emitter.onTimeout {
//            eventEmitter.unregister(emitter)
//            emitter.complete()
//        }
//        emitter.onError { _ -> eventEmitter.unregister(emitter) }
        return emitter
    }
}
