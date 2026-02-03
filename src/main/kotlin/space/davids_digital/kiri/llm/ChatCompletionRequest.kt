package space.davids_digital.kiri.llm

data class ChatCompletionRequest (
    val modelHandle: String,
    val instructions: String,
    val messages: List<Message>,
    val maxOutputTokens: Long,
    val temperature: Double,
    val tools: Tools,
    val reasoning: Reasoning
) {
    data class Message (val role: Role, val content: List<ContentItem>) {
        enum class Role { USER, ASSISTANT }
        sealed interface ContentItem {
            data class Text (val text: String) : ContentItem
            data class Image (val data: ByteArray, val mediaType: ChatCompletionImageType) : ContentItem {
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (javaClass != other?.javaClass) return false

                    other as Image

                    if (!data.contentEquals(other.data)) return false
                    if (mediaType != other.mediaType) return false

                    return true
                }

                override fun hashCode(): Int {
                    var result = data.contentHashCode()
                    result = 31 * result + mediaType.hashCode()
                    return result
                }
            }

            data class ToolUse (val toolUse: ChatCompletionToolUse) : ContentItem
            data class ToolResult (val toolResult: ChatCompletionToolUseResult) : ContentItem
            data class Reasoning (val id: String?, val content: String?, val signature: String) : ContentItem
            data class RedactedReasoning (val data: String) : ContentItem
        }
    }

    data class Tools(
        val choice: ToolChoice,
        val allowParallelUse: Boolean,
        val functions: List<Function>,
        val external: External
    ) {
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
        data class External (val webSearch: WebSearch) {
            data class WebSearch (
                val enabled: Boolean,
            )
        }
    }

    data class Reasoning(
        val enabled: Boolean,
        val maxTokens: Long,
        val effort: Effort
    ) {
        enum class Effort { AUTO, LOW, MEDIUM, HIGH }
    }
}