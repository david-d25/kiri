package space.davids_digital.kiri.agent.frame

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedDeque

@Component
class FrameBuffer : Iterable<Frame> {
    private val frames = ConcurrentLinkedDeque<Frame>()

    private var sequenceCounter: Long = 0
    private val lock = Any()

    private val updatesInternal = MutableSharedFlow<Long>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val updates = updatesInternal.asSharedFlow()

    var hardLimit = 32

    val size get() = frames.size

    data class Snapshot(
        val frames: List<Frame>,
        val sequence: Long
    )

    fun snapshot(): Snapshot = synchronized(lock) {
        Snapshot(frames.toList(), sequenceCounter)
    }

    fun clear() = synchronized(lock) {
        frames.clear()
        incrementSequenceCounter()
    }

    fun trim(keepLastN: Int) {
        synchronized(lock) {
            while (frames.size > keepLastN) {
                frames.poll()
            }
            incrementSequenceCounter()
        }
    }

    fun add(frame: Frame) = synchronized(lock) {
        frames.add(frame)
        trimLocked()
        incrementSequenceCounter()
    }

    fun addStatic(block: StaticDataFrame.Builder.() -> Unit) {
        val builder = StaticDataFrame.Builder().apply(block)
        add(builder.build())
    }

    fun addToolCall(block: ToolCallFrame.Builder.() -> Unit) {
        val builder = ToolCallFrame.Builder().apply(block)
        add(builder.build())
    }

    override fun iterator(): Iterator<Frame> = sequence {
        yieldAll(frames)
    }.iterator()

    private fun trimLocked() {
        while (frames.size > hardLimit) {
            frames.poll()
        }
    }

    private fun incrementSequenceCounter() {
        sequenceCounter++
        updatesInternal.tryEmit(sequenceCounter)
    }
}