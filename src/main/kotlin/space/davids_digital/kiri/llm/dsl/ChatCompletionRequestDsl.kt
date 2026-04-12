package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.ChatCompletionImageType
import space.davids_digital.kiri.llm.ChatCompletionRequest
import space.davids_digital.kiri.llm.ChatCompletionRequest.Message.ContentItem.ToolResult
import space.davids_digital.kiri.llm.ChatCompletionWebSearch

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
        emptyList(),
        ChatCompletionRequest.Tools.External(
            webSearch = ChatCompletionRequest.Tools.External.WebSearch(
                enabled = false
            )
        )
    )
    var reasoning: ChatCompletionRequest.Reasoning = ChatCompletionRequest.Reasoning(
        enabled = false,
        maxTokens = 0,
        effort = ChatCompletionRequest.Reasoning.Effort.AUTO
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

    fun reasoning(block: ChatCompletionRequestReasoningBuilder.() -> Unit) {
        reasoning = ChatCompletionRequestReasoningBuilder().apply(block).build()
    }

    fun build(): ChatCompletionRequest {
        requireNotNull(modelHandle) { "modelHandle must be set" }
        return ChatCompletionRequest(
            modelHandle!!,
            instructions,
            messages,
            maxOutputTokens,
            temperature,
            tools,
            reasoning
        )
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

    fun toolUse(block: ChatCompletionToolUseBuilder.() -> Unit) {
        content.add(ChatCompletionRequest.Message.ContentItem.ToolUse(ChatCompletionToolUseBuilder().apply(block).build()))
    }

    fun toolResult(block: ChatCompletionToolUseResultBuilder.() -> Unit) {
        content.add(ToolResult(ChatCompletionToolUseResultBuilder().apply(block).build()))
    }

    fun reasoning(id: String?, reasoningContent: String, signature: String) {
        content.add(ChatCompletionRequest.Message.ContentItem.Reasoning(id, reasoningContent, signature))
    }

    fun redactedReasoning(data: String) {
        content.add(ChatCompletionRequest.Message.ContentItem.RedactedReasoning(data))
    }

    fun webSearch(webSearch: ChatCompletionWebSearch) {
        content.add(webSearch)
    }

    fun build(): ChatCompletionRequest.Message {
        return ChatCompletionRequest.Message(role, content)
    }
}

@ChatCompletionRequestDsl
class ChatCompletionRequestToolsBuilder {
    var choice: ChatCompletionRequest.Tools.ToolChoice = ChatCompletionRequest.Tools.ToolChoice.AUTO
    var allowParallelUse: Boolean = true
    var functions: MutableList<ChatCompletionRequest.Tools.Function> = mutableListOf()
    var external: ChatCompletionRequest.Tools.External = ChatCompletionRequest.Tools.External(
        webSearch = ChatCompletionRequest.Tools.External.WebSearch(
            enabled = false
        )
    )

    fun function(block: ChatCompletionRequestToolsFunctionBuilder.() -> Unit) {
        functions.add(ChatCompletionRequestToolsFunctionBuilder().apply(block).build())
    }

    fun external(block: ChatCompletionRequestToolsExternalBuilder.() -> Unit) {
        external = ChatCompletionRequestToolsExternalBuilder().apply(block).build()
    }

    fun build(): ChatCompletionRequest.Tools {
        return ChatCompletionRequest.Tools(choice, allowParallelUse, functions, external)
    }
}

@ChatCompletionRequestDsl
class ChatCompletionRequestReasoningBuilder {
    var enabled: Boolean = false
    var maxTokens: Long = 0
    var effort = ChatCompletionRequest.Reasoning.Effort.AUTO

    fun build(): ChatCompletionRequest.Reasoning {
        return ChatCompletionRequest.Reasoning(enabled, maxTokens, effort)
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
class ChatCompletionRequestToolsExternalBuilder {
    var webSearch: ChatCompletionRequest.Tools.External.WebSearch =
        ChatCompletionRequest.Tools.External.WebSearch(enabled = false)

    fun webSearch(block: ChatCompletionRequestToolsExternalWebSearchBuilder.() -> Unit) {
        webSearch = ChatCompletionRequestToolsExternalWebSearchBuilder().apply(block).build()
    }

    fun build(): ChatCompletionRequest.Tools.External {
        return ChatCompletionRequest.Tools.External(webSearch)
    }
}

@ChatCompletionRequestDsl
class ChatCompletionRequestToolsExternalWebSearchBuilder {
    var enabled: Boolean = false

    fun build(): ChatCompletionRequest.Tools.External.WebSearch {
        return ChatCompletionRequest.Tools.External.WebSearch(enabled)
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