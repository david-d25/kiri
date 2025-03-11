package space.davids_digital.kiri.llm

data class LlmMessageResponse (
    val content: List<ContentItem>,
    val id: String,
    val stopReason: StopReason?,
    val usage: Usage,
) {
    sealed class ContentItem {
        data class Text(val text: String) : ContentItem()
        data class ToolUse(val id: String, val name: String, val input: Input) : ContentItem() {
            sealed class Input {
                data class TextValue(val text: String) : Input()
                data class NumberValue(val number: Double) : Input()
                data class BooleanValue(val boolean: Boolean) : Input()
                data class ArrayValue(val items: List<Input>) : Input()
                data class ObjectValue(val items: Map<String, Input>) : Input()
            }
        }
    }
    enum class StopReason { END_TURN, MAX_TOKENS, STOP_SEQUENCE, TOOL_USE, UNKNOWN }
    data class Usage (val inputTokens: Long, val outputTokens: Long)
}