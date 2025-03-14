package space.davids_digital.kiri.agent.frame

import java.util.LinkedList

class FrameBuffer : Iterable<Frame> {
    private val frames = LinkedList<Frame>()

    fun clear() {
        frames.clear()
    }

    fun add(frame: Frame) {
        frames.add(frame)
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
        return frames.iterator()
    }
}