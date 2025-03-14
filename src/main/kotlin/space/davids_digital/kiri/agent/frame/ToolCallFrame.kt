package space.davids_digital.kiri.agent.frame

import space.davids_digital.kiri.llm.LlmToolUse
import space.davids_digital.kiri.llm.LlmToolUseResult

class ToolCallFrame(
    val useId: String,
    val name: String,
    val toolUse: LlmToolUse,
    val result: LlmToolUseResult
) : Frame() {
    class Builder {
        var useId: String = ""
        var name: String = ""
        lateinit var toolUse: LlmToolUse
        lateinit var result: LlmToolUseResult

        fun build() = ToolCallFrame(useId, name, toolUse, result)
    }
}