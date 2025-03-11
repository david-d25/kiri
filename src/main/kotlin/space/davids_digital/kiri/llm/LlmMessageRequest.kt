package space.davids_digital.kiri.llm

import com.fasterxml.jackson.databind.JsonNode

data class LlmMessageRequest<MODEL> (
    val model: MODEL,
    val system: String,
    val messages: List<Message>,
    val maxOutputTokens: Long,
    val temperature: Double,
    val tools: Tools,
) {
    data class Message (val role: Role, val content: List<ContentItem>) {
        enum class Role { USER, ASSISTANT }
        sealed interface ContentValueItem
        sealed class ContentItem {
            data class Text (val text: String) : ContentItem(), ContentValueItem
            data class Image (val data: ByteArray, val mediaType: MediaType) : ContentItem(), ContentValueItem
            data class ToolUse (val id: String, val name: String, val input: JsonNode) : ContentItem()
            data class ToolResult (val toolUseId: String, val content: List<ContentValueItem>) : ContentItem()
            enum class MediaType { JPEG, PNG, GIF, WEBP }
        }
    }
    data class Tools (val choice: ToolChoice, val allowParallelUse: Boolean, val functions: List<Function>) {
        enum class ToolChoice { AUTO, NONE, REQUIRED }
        data class Function (val name: String, val description: String, val parameters: ParameterValue.ObjectValue) {
            sealed class ParameterValue {
                data class ObjectValue (
                    val description: String?,
                    val properties: Map<String, ParameterValue>,
                    val required: List<String>
                ) : ParameterValue()
                data class StringValue (val description: String?, val enum: List<String>?) : ParameterValue()
                data class NumberValue (val description: String?) : ParameterValue()
                data class BooleanValue (val description: String?) : ParameterValue()
            }
        }
    }
}