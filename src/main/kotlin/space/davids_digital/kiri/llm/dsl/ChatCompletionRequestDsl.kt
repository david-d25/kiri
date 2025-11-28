package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.ChatCompletionImageType
import space.davids_digital.kiri.llm.ChatCompletionRequest
import space.davids_digital.kiri.llm.ChatCompletionRequest.Message.ContentItem.ToolResult

@DslMarker
annotation class ChatCompletionRequestDsl

fun chatCompletionRequest(block: ChatCompletionRequestBuilder.() -> Unit) =
    ChatCompletionRequestBuilder().apply(block).build()

@ChatCompletionRequestDsl
class ChatCompletionRequestBuilder {
    var modelHandle: String? = null
    var instructions: String = ""
    var messages: MutableList<ChatCompletionRequest.Message> = mutableListOf()
    var maxOutputTokens: Long = 0
    var temperature: Double = 0.0
    var tools: ChatCompletionRequest.Tools = ChatCompletionRequest.Tools(
        ChatCompletionRequest.Tools.ToolChoice.AUTO,
        false,
        emptyList()
    )

    fun message(block: ChatCompletionRequestMessageBuilder.() -> Unit) {
        messages.add(ChatCompletionRequestMessageBuilder().apply(block).build())
    }

    fun userMessage(block: ChatCompletionRequestUserMessageBuilder.() -> Unit) {
        messages.add(ChatCompletionRequestUserMessageBuilder().apply(block).build())
    }

    fun assistantMessage(block: ChatCompletionRequestAssistantMessageBuilder.() -> Unit) {
        messages.add(ChatCompletionRequestAssistantMessageBuilder().apply(block).build())
    }

    fun tools(block: ChatCompletionRequestToolsBuilder.() -> Unit) {
        tools = ChatCompletionRequestToolsBuilder().apply(block).build()
    }

    fun build(): ChatCompletionRequest {
        requireNotNull(modelHandle) { "modelHandle must be set" }
        return ChatCompletionRequest(modelHandle!!, instructions, messages, maxOutputTokens, temperature, tools)
    }
}

@ChatCompletionRequestDsl
open class ChatCompletionRequestMessageBuilder {
    var role: ChatCompletionRequest.Message.Role = ChatCompletionRequest.Message.Role.USER
    var content: MutableList<ChatCompletionRequest.Message.ContentItem> = mutableListOf()

    fun text(text: String) {
        content.add(ChatCompletionRequest.Message.ContentItem.Text(text))
    }

    fun line(text: String) {
        content.add(ChatCompletionRequest.Message.ContentItem.Text(text + "\n"))
    }

    fun image(data: ByteArray, type: ChatCompletionImageType) {
        content.add(ChatCompletionRequest.Message.ContentItem.Image(data, type))
    }

    fun toolUse(block: LlmToolUseBuilder.() -> Unit) {
        content.add(ChatCompletionRequest.Message.ContentItem.ToolUse(LlmToolUseBuilder().apply(block).build()))
    }

    fun toolResult(block: ChatCompletionToolUseResultBuilder.() -> Unit) {
        content.add(ToolResult(ChatCompletionToolUseResultBuilder().apply(block).build()))
    }

    fun build(): ChatCompletionRequest.Message {
        return ChatCompletionRequest.Message(role, content)
    }
}

@ChatCompletionRequestDsl
class ChatCompletionRequestToolsBuilder {
    var choice: ChatCompletionRequest.Tools.ToolChoice = ChatCompletionRequest.Tools.ToolChoice.AUTO
    var allowParallelUse: Boolean = false
    var functions: MutableList<ChatCompletionRequest.Tools.Function> = mutableListOf()

    fun function(block: ChatCompletionRequestToolsFunctionBuilder.() -> Unit) {
        functions.add(ChatCompletionRequestToolsFunctionBuilder().apply(block).build())
    }

    fun build(): ChatCompletionRequest.Tools {
        return ChatCompletionRequest.Tools(choice, allowParallelUse, functions)
    }
}

@ChatCompletionRequestDsl
class ChatCompletionRequestToolsFunctionBuilder {
    var name: String = ""
    var description: String? = null
    var parameters: ChatCompletionRequest.Tools.Function.ParameterValue.ObjectValue =
        ChatCompletionRequest.Tools.Function.ParameterValue.ObjectValue(
            null,
            emptyMap(),
            emptyList()
        )

    fun build(): ChatCompletionRequest.Tools.Function {
        return ChatCompletionRequest.Tools.Function(name, description, parameters)
    }
}

@ChatCompletionRequestDsl
class ChatCompletionRequestUserMessageBuilder : ChatCompletionRequestMessageBuilder() {
    init {
        role = ChatCompletionRequest.Message.Role.USER
    }
}

@ChatCompletionRequestDsl
class ChatCompletionRequestAssistantMessageBuilder : ChatCompletionRequestMessageBuilder() {
    init {
        role = ChatCompletionRequest.Message.Role.ASSISTANT
    }
}