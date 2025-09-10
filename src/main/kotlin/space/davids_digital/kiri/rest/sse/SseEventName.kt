package space.davids_digital.kiri.rest.sse

import space.davids_digital.kiri.rest.dto.EngineStateDto
import space.davids_digital.kiri.rest.dto.FrameBufferStateDto

@JvmInline
value class SseEventKey<T : Any?>(val value: String)

object SseEventName {
    val EngineState = SseEventKey<EngineStateDto>("engineState")
    val FrameBufferState = SseEventKey<FrameBufferStateDto>("frameBufferState")
    val Heartbeat   = SseEventKey<Unit>("heartbeat")
}