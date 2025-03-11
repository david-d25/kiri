package space.davids_digital.kiri.llm

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

@DslMarker
annotation class LlmMessageRequestDsl

fun <MODEL> llmMessageRequest(block: LlmMessageRequestBuilder<MODEL>.() -> Unit) =
    LlmMessageRequestBuilder<MODEL>().apply(block).build()

@LlmMessageRequestDsl
class LlmMessageRequestBuilder<MODEL> {
    var model: MODEL? = null
    var system: String = ""
    var messages: MutableList<LlmMessageRequest.Message> = mutableListOf()
    var maxOutputTokens: Long = 0
    var temperature: Double = 0.0
    var tools: LlmMessageRequest.Tools = LlmMessageRequest.Tools(
        LlmMessageRequest.Tools.ToolChoice.AUTO,
        false,
        emptyList()
    )

    fun userMessage(block: LlmMessageRequestUserMessageBuilder.() -> Unit) {
        messages.add(LlmMessageRequestUserMessageBuilder().apply(block).build())
    }

    fun assistantMessage(block: LlmMessageRequestAssistantMessageBuilder.() -> Unit) {
        messages.add(LlmMessageRequestAssistantMessageBuilder().apply(block).build())
    }

    fun tools(block: LlmMessageRequestToolsBuilder.() -> Unit) {
        tools = LlmMessageRequestToolsBuilder().apply(block).build()
    }

    fun build(): LlmMessageRequest<Any> {
        requireNotNull(model) { "model must be set" }
        return LlmMessageRequest(model!!, system, messages, maxOutputTokens, temperature, tools)
    }
}

@LlmMessageRequestDsl
open class LlmMessageRequestMessageBuilder {
    var role: LlmMessageRequest.Message.Role = LlmMessageRequest.Message.Role.USER
    var content: MutableList<LlmMessageRequest.Message.ContentItem> = mutableListOf()

    fun text(text: String) {
        content.add(LlmMessageRequest.Message.ContentItem.Text(text))
    }

    fun image(data: ByteArray, mediaType: LlmMessageRequest.Message.ContentItem.MediaType) {
        content.add(LlmMessageRequest.Message.ContentItem.Image(data, mediaType))
    }

    fun toolUse(block: LlmMessageRequestMessageToolUseBuilder.() -> Unit) {
        content.add(LlmMessageRequestMessageToolUseBuilder().apply(block).build())
    }

    fun toolResult(block: LlmMessageRequestMessageToolResultBuilder.() -> Unit) {
        content.add(LlmMessageRequestMessageToolResultBuilder().apply(block).build())
    }

    fun build(): LlmMessageRequest.Message {
        return LlmMessageRequest.Message(role, content)
    }
}

@LlmMessageRequestDsl
class LlmMessageRequestToolsBuilder {
    var choice: LlmMessageRequest.Tools.ToolChoice = LlmMessageRequest.Tools.ToolChoice.AUTO
    var allowParallelUse: Boolean = false
    var functions: MutableList<LlmMessageRequest.Tools.Function> = mutableListOf()

    fun function(block: LlmMessageRequestToolsFunctionBuilder.() -> Unit) {
        functions.add(LlmMessageRequestToolsFunctionBuilder().apply(block).build())
    }

    fun build(): LlmMessageRequest.Tools {
        return LlmMessageRequest.Tools(choice, allowParallelUse, functions)
    }
}

@LlmMessageRequestDsl
class LlmMessageRequestToolsFunctionBuilder {
    var name: String = ""
    var description: String = ""
    var parameters: LlmMessageRequest.Tools.Function.ParameterValue.ObjectValue =
        LlmMessageRequest.Tools.Function.ParameterValue.ObjectValue(
            null,
            emptyMap(),
            emptyList()
        )

    fun build(): LlmMessageRequest.Tools.Function {
        return LlmMessageRequest.Tools.Function(name, description, parameters)
    }
}

@LlmMessageRequestDsl
class LlmMessageRequestMessageToolUseBuilder {
    var id: String = ""
    var name: String = ""
    var input: JsonNode = ObjectNode(JsonNodeFactory.instance)

    fun build(): LlmMessageRequest.Message.ContentItem.ToolUse {
        return LlmMessageRequest.Message.ContentItem.ToolUse(id, name, input)
    }
}

@LlmMessageRequestDsl
class LlmMessageRequestMessageToolResultBuilder {
    var toolUseId: String = ""
    var content: MutableList<LlmMessageRequest.Message.ContentValueItem> = mutableListOf()

    fun text(text: String) {
        content.add(LlmMessageRequest.Message.ContentItem.Text(text))
    }

    fun image(data: ByteArray, mediaType: LlmMessageRequest.Message.ContentItem.MediaType) {
        content.add(LlmMessageRequest.Message.ContentItem.Image(data, mediaType))
    }

    fun build(): LlmMessageRequest.Message.ContentItem.ToolResult {
        return LlmMessageRequest.Message.ContentItem.ToolResult(toolUseId, content)
    }
}

@LlmMessageRequestDsl
class LlmMessageRequestUserMessageBuilder : LlmMessageRequestMessageBuilder() {
    init {
        role = LlmMessageRequest.Message.Role.USER
    }
}

@LlmMessageRequestDsl
class LlmMessageRequestAssistantMessageBuilder : LlmMessageRequestMessageBuilder() {
    init {
        role = LlmMessageRequest.Message.Role.ASSISTANT
    }
}