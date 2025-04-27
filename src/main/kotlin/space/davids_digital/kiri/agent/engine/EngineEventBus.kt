package space.davids_digital.kiri.agent.engine

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Service

@Service
class EngineEventBus {
    private val eventFlow = MutableSharedFlow<EngineEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    suspend fun publish(event: EngineEvent) {
        eventFlow.emit(event)
    }

    fun subscribe(): Flow<EngineEvent> {
        return eventFlow.asSharedFlow()
    }
    /**
     * Non-blocking wake-up signal: interrupts agent sleep if sleeping.
     * Returns true if the event was emitted, false otherwise.
     */
    fun fireWakeUp(): Boolean = eventFlow.tryEmit(EngineEvent())
}