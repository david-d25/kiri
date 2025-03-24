package space.davids_digital.kiri.agent.engine

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Service

@Service
class EngineEventBus {
    private val eventFlow = MutableSharedFlow<EngineEvent>(
        extraBufferCapacity = Channel.BUFFERED,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    suspend fun publishEvent(event: EngineEvent) {
        eventFlow.emit(event)
    }

    fun subscribeToEvents(): Flow<EngineEvent> {
        return eventFlow.asSharedFlow()
    }
}