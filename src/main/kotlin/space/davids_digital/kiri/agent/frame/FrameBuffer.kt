package space.davids_digital.kiri.agent.frame

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class FrameBuffer : Iterable<Frame> {
    private val fixedFrames = ConcurrentLinkedQueue<DataFrame>()
    private val rollingFrames = ConcurrentLinkedQueue<Frame>()

    var hardLimit = 16

    val onlyFixed get() = fixedFrames.iterator()
    val onlyRolling get() = rollingFrames.iterator()

    fun clearAll() {
        clearOnlyFixed()
        clearOnlyRolling()
    }

    fun clearOnlyFixed() {
        fixedFrames.clear()
    }

    fun clearOnlyRolling() {
        rollingFrames.clear()
    }

    fun add(frame: Frame) {
        rollingFrames.add(frame)
        trim()
    }

    fun addFixed(frame: DataFrame) {
        fixedFrames.add(frame)
        trim()
    }

    fun removeFixed(frame: DataFrame) {
        fixedFrames.remove(frame)
    }

    fun addStatic(block: StaticDataFrame.Builder.() -> Unit) {
        val builder = StaticDataFrame.Builder()
        builder.block()
        add(builder.build())
    }

    fun addToolCall(block: ToolCallFrame.Builder.() -> Unit) {
        val builder = ToolCallFrame.Builder()
        builder.block()
        add(builder.build())
    }

    override fun iterator(): Iterator<Frame> {
        return sequence {
            yieldAll(fixedFrames)
            yieldAll(rollingFrames)
        }.iterator()
    }

    private fun trim() {
        while (rollingFrames.isNotEmpty() && fixedFrames.size + rollingFrames.size > hardLimit) {
            rollingFrames.poll()
        }
    }
}