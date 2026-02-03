package space.davids_digital.kiri.llm

data class ChatCompletionResponse (
    val content: List<ContentItem>,
    val id: String,
    val stopReason: StopReason?,
    val usage: Usage,
) {
    sealed interface ContentItem {
        data class Text(val text: String) : ContentItem
        data class ToolUse(val toolUse: ChatCompletionToolUse) : ContentItem
        data class Reasoning(val id: String?, val content: String?, val signature: String) : ContentItem
        data class RedactedReasoning(val data: String) : ContentItem
    }
    enum class StopReason { END_TURN, MAX_TOKENS, STOP_SEQUENCE, TOOL_USE, UNKNOWN }
    data class Usage (val inputTokens: Long, val completionTokens: Long)
}