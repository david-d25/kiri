package space.davids_digital.kiri.agent.frame

import org.springframework.stereotype.Component
import space.davids_digital.kiri.rest.dto.FrameAddedEventDto
import space.davids_digital.kiri.rest.service.AdminEventEmitterService
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class FrameBuffer(
    private val adminEventEmitter: AdminEventEmitterService
) : Iterable<Frame> {
    private val fixedFrames = ConcurrentLinkedQueue<DataFrame>()
    private val rollingFrames = ConcurrentLinkedQueue<Frame>()

    var hardLimit = 16

    val onlyFixed get() = fixedFrames.iterator()
    val onlyRolling get() = rollingFrames.iterator()
    val size get() = fixedFrames.size + rollingFrames.size

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

    private var sequenceCounter: Long = 0

    fun addRolling(frame: Frame) {
        rollingFrames.add(frame)
        publishFrameAdded()
        trim()
    }

    fun removeRolling(frame: Frame) {
        rollingFrames.remove(frame)
    }

    fun addFixed(frame: DataFrame) {
        fixedFrames.add(frame)
        publishFrameAdded()
        trim()
    }

    fun removeFixed(frame: DataFrame) {
        fixedFrames.remove(frame)
    }

    fun addStatic(block: StaticDataFrame.Builder.() -> Unit) {
        val builder = StaticDataFrame.Builder()
        builder.block()
        addRolling(builder.build())
    }

    fun addToolCall(block: ToolCallFrame.Builder.() -> Unit) {
        val builder = ToolCallFrame.Builder()
        builder.block()
        addRolling(builder.build())
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

    private fun publishFrameAdded() {
        val event = FrameAddedEventDto(sequenceCounter++, System.currentTimeMillis())
        adminEventEmitter.push(event)
    }
}