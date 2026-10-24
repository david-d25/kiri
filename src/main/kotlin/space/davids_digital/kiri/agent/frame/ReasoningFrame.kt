package space.davids_digital.kiri.agent.frame

/**
 * [epoch] is the [FrameBuffer.epoch] the reasoning was produced in — see [FrameBuffer.isStale].
 */
class ReasoningFrame(
    val content: String,
    val signature: String,
    val reasoningId: String?,
    val epoch: Long,
) : Frame()
