package space.davids_digital.kiri.agent.frame

import space.davids_digital.kiri.llm.LlmToolUse
import space.davids_digital.kiri.llm.LlmToolUseResult

class ToolCallFrame(
    val toolUse: LlmToolUse,
    val resultProvider: () -> LlmToolUseResult
) : Frame() {
    class Builder {
        lateinit var toolUse: LlmToolUse
        lateinit var resultProvider: () -> LlmToolUseResult

        fun build() = ToolCallFrame(toolUse, resultProvider)
    }
}