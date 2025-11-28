package space.davids_digital.kiri.agent.frame

import space.davids_digital.kiri.llm.ChatCompletionToolUse
import space.davids_digital.kiri.llm.ChatCompletionToolUseResult

class ToolCallFrame(
    val toolUse: ChatCompletionToolUse,
    val resultProvider: () -> ChatCompletionToolUseResult
) : Frame() {
    class Builder {
        lateinit var toolUse: ChatCompletionToolUse
        lateinit var resultProvider: () -> ChatCompletionToolUseResult

        fun build() = ToolCallFrame(toolUse, resultProvider)
    }
}