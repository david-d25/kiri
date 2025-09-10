package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.LlmImageType
import space.davids_digital.kiri.llm.LlmMessageRequest
import space.davids_digital.kiri.llm.LlmMessageRequest.Message.ContentItem.ToolResult

@DslMarker
annotation class LlmMessageRequestDsl

suspend fun llmMessageRequest(block: suspend LlmMessageRequestBuilder.() -> Unit) =
    LlmMessageRequestBuilder().apply { block() }.build()

@LlmMessageRequestDsl
class LlmMessageRequestBuilder {
    var model: String? = null
    var system: String = ""
    var messages: MutableList<LlmMessageRequest.Message> = mutableListOf()
    var maxOutputTokens: Long = 0
    var temperature: Double = 0.0
    var tools: LlmMessageRequest.Tools = LlmMessageRequest.Tools(
        LlmMessageRequest.Tools.ToolChoice.AUTO,
        false,
        emptyList()
    )

    suspend fun userMessage(block: suspend LlmMessageRequestUserMessageBuilder.() -> Unit) {
        messages.add(LlmMessageRequestUserMessageBuilder().apply { block() }.build())
    }

    suspend fun assistantMessage(block: suspend LlmMessageRequestAssistantMessageBuilder.() -> Unit) {
        messages.add(LlmMessageRequestAssistantMessageBuilder().apply { block() }.build())
    }

    suspend fun tools(block: suspend LlmMessageRequestToolsBuilder.() -> Unit) {
        tools = LlmMessageRequestToolsBuilder().apply { block() }.build()
    }

    fun build(): LlmMessageRequest {
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

    fun image(data: ByteArray, type: LlmImageType) {
        content.add(LlmMessageRequest.Message.ContentItem.Image(data, type))
    }

    fun toolUse(block: LlmToolUseBuilder.() -> Unit) {
        content.add(LlmMessageRequest.Message.ContentItem.ToolUse(LlmToolUseBuilder().apply(block).build()))
    }

    fun toolResult(block: LlmToolUseResultBuilder.() -> Unit) {
        content.add(ToolResult(LlmToolUseResultBuilder().apply(block).build()))
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
    var description: String? = null
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