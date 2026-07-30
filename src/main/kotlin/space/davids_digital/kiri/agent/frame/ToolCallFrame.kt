package space.davids_digital.kiri.agent.frame

import space.davids_digital.kiri.llm.ChatCompletionToolUse
import space.davids_digital.kiri.llm.ChatCompletionToolUseResult
import space.davids_digital.kiri.agent.engine.AgentEngine

class ToolCallFrame(
    val toolUse: ChatCompletionToolUse,
    val resultProvider: () -> ChatCompletionToolUseResult
) : Frame() {
    class Builder {
        lateinit var toolUse: ChatCompletionToolUse
        lateinit var resultProvider: () -> ChatCompletionToolUseResult

        fun build() = ToolCallFrame(toolUse, resultProvider)
    }

    companion object {
        /**
         * Formats a tool call result into [ChatCompletionToolUseResult].
         * Shared between [AgentEngine], [SyntheticToolCallTracker], and admin tool execution.
         */
        fun formatResult(callId: String, toolName: String, result: Any?): ChatCompletionToolUseResult {
            val output = when {
                result === Unit || result == null ->
                    listOf(ChatCompletionToolUseResult.Output.Text("ok"))
                result is List<*> && result.all { it is DataFrame.ContentPart } ->
                    result.filterIsInstance<DataFrame.ContentPart>().map {
                        when (it) {
                            is DataFrame.Text -> ChatCompletionToolUseResult.Output.Text(it.text)
                            is DataFrame.Image -> ChatCompletionToolUseResult.Output.Image(it.data, it.type)
                        }
                    }
                else -> listOf(ChatCompletionToolUseResult.Output.Text(result.toString()))
            }
            return ChatCompletionToolUseResult(callId, toolName, output)
        }
    }
}