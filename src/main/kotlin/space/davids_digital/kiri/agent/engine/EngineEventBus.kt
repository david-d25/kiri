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

    fun tryPublish(event: EngineEvent): Boolean {
        return eventFlow.tryEmit(event)
    }

    fun subscribe(): Flow<EngineEvent> {
        return eventFlow.asSharedFlow()
    }
}