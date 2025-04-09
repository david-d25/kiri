package space.davids_digital.kiri.llm

data class LlmMessageRequest (
    val model: String,
    val system: String,
    val messages: List<Message>,
    val maxOutputTokens: Long,
    val temperature: Double,
    val tools: Tools,
) {
    data class Message (val role: Role, val content: List<ContentItem>) {
        enum class Role { USER, ASSISTANT }
        sealed class ContentItem {
            data class Text (val text: String) : ContentItem()
            data class Image (val data: ByteArray, val mediaType: LlmImageType) : ContentItem()
            data class ToolUse (val toolUse: LlmToolUse) : ContentItem()
            data class ToolResult (val toolResult: LlmToolUseResult) : ContentItem()
        }
    }
    data class Tools (val choice: ToolChoice, val allowParallelUse: Boolean, val functions: List<Function>) {
        enum class ToolChoice { AUTO, NONE, REQUIRED }
        data class Function (val name: String, val description: String?, val parameters: ParameterValue.ObjectValue?) {
            sealed class ParameterValue {
                data class ObjectValue (
                    val description: String?,
                    val properties: Map<String, ParameterValue>,
                    val required: List<String>
                ) : ParameterValue()
                data class ArrayValue (val description: String?, val items: ParameterValue) : ParameterValue()
                data class StringValue (val description: String?, val enum: List<String>?) : ParameterValue()
                data class NumberValue (val description: String?) : ParameterValue()
                data class BooleanValue (val description: String?) : ParameterValue()
            }
        }
    }
}