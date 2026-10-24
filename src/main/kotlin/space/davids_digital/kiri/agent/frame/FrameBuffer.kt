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

    /**
     * Once [hardLimit] is exceeded, frames are dropped in one batch down to this size rather than one
     * per append: between trims the history is append-only, which keeps reasoning replayable (see
     * [epoch]) and the prompt cache warm.
     */
    val trimTarget get() = hardLimit * 2 / 3

    /**
     * Bumped whenever the history before the newest frame changes — a front trim, a clear, or an
     * outside change such as a different tool set ([invalidateReasoning]). Some providers (Anthropic,
     * from Claude Sonnet 5.5) bind reasoning to the exact history, system prompt and tools it was
     * produced with and reject it once any of those change.
     */
    val epoch get() = epochInternal

    @Volatile
    private var epochInternal: Long = 0

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
        epochInternal++
        incrementSequenceCounter()
    }

    fun trim(keepLastN: Int) {
        synchronized(lock) {
            if (frames.size > keepLastN) {
                while (frames.size > keepLastN) {
                    frames.poll()
                }
                epochInternal++
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

    fun addReasoning(content: String, signature: String, reasoningId: String?) = synchronized(lock) {
        add(ReasoningFrame(content, signature, reasoningId, epoch))
    }

    fun invalidateReasoning() = synchronized(lock) {
        epochInternal++
    }

    fun isStale(frame: ReasoningFrame) = frame.epoch < epoch

    override fun iterator(): Iterator<Frame> = sequence {
        yieldAll(frames)
    }.iterator()

    private fun trimLocked() {
        if (frames.size <= hardLimit) return
        while (frames.size > trimTarget) {
            frames.poll()
        }
        epochInternal++
    }

    private fun incrementSequenceCounter() {
        sequenceCounter++
        updatesInternal.tryEmit(sequenceCounter)
    }
}
