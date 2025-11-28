package space.davids_digital.kiri.llm

data class ChatCompletionResponse (
    val content: List<ContentItem>,
    val id: String,
    val stopReason: StopReason?,
    val usage: Usage,
) {
    sealed class ContentItem {
        data class Text(val text: String) : ContentItem()
        data class ToolUse(val toolUse: ChatCompletionToolUse) : ContentItem()
    }
    enum class StopReason { END_TURN, MAX_TOKENS, STOP_SEQUENCE, TOOL_USE, UNKNOWN }
    data class Usage (val inputTokens: Long, val outputTokens: Long)
}