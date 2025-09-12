package space.davids_digital.kiri.agent.frame

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class FrameBuffer : Iterable<Frame> {
    private val fixedFrames = ConcurrentLinkedQueue<DataFrame>()
    private val rollingFrames = ConcurrentLinkedQueue<Frame>()

    private var sequenceCounter: Long = 0
    private val lock = Any()

    private val updatesInternal = MutableSharedFlow<Long>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val updates = updatesInternal.asSharedFlow()

    var hardLimit = 16

    val onlyFixed get() = fixedFrames.iterator()
    val onlyRolling get() = rollingFrames.iterator()
    val size get() = fixedFrames.size + rollingFrames.size

    data class Snapshot(
        val fixed: List<DataFrame>,
        val rolling: List<Frame>,
        val hardLimit: Int,
        val sequence: Long
    )

    fun snapshot(): Snapshot = synchronized(lock) {
        Snapshot(
            fixed = fixedFrames.toList(),
            rolling = rollingFrames.toList(),
            hardLimit = hardLimit,
            sequence = sequenceCounter
        )
    }

    fun notifyChanged() = synchronized(lock) {
        bumpLocked()
    }

    fun clearAll() = synchronized(lock) {
        clearOnlyFixedLocked()
        clearOnlyRollingLocked()
        bumpLocked()
    }

    fun clearOnlyFixed() = synchronized(lock) {
        clearOnlyFixedLocked()
        bumpLocked()
    }
    private fun clearOnlyFixedLocked() {
        fixedFrames.clear()
    }

    fun clearOnlyRolling() = synchronized(lock) {
        clearOnlyRollingLocked()
        bumpLocked()
    }

    private fun clearOnlyRollingLocked() {
        rollingFrames.clear()
    }

    fun addRolling(frame: Frame) = synchronized(lock) {
        rollingFrames.add(frame)
        trimLocked()
        bumpLocked()
    }

    fun removeRolling(frame: Frame) = synchronized(lock) {
        rollingFrames.remove(frame)
        bumpLocked()
    }

    fun addFixed(frame: DataFrame) = synchronized(lock) {
        fixedFrames.add(frame)
        trimLocked()
        bumpLocked()
    }

    fun removeFixed(frame: DataFrame) = synchronized(lock) {
        fixedFrames.remove(frame); bumpLocked()
    }

    fun addStatic(block: StaticDataFrame.Builder.() -> Unit) {
        val builder = StaticDataFrame.Builder().apply(block)
        addRolling(builder.build())
    }

    fun addToolCall(block: ToolCallFrame.Builder.() -> Unit) {
        val builder = ToolCallFrame.Builder().apply(block)
        addRolling(builder.build())
    }

    override fun iterator(): Iterator<Frame> = sequence {
        yieldAll(fixedFrames)
        yieldAll(rollingFrames)
    }.iterator()

    private fun trimLocked() {
        while (rollingFrames.isNotEmpty() && fixedFrames.size + rollingFrames.size > hardLimit) {
            rollingFrames.poll()
        }
    }

    private fun bumpLocked() {
        sequenceCounter++
        updatesInternal.tryEmit(sequenceCounter)
    }
}