package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.ChatCompletionResponse
import space.davids_digital.kiri.llm.ChatCompletionResponse.ContentItem
import space.davids_digital.kiri.llm.ChatCompletionResponse.ContentItem.ToolUse
import space.davids_digital.kiri.llm.ChatCompletionResponse.StopReason
import space.davids_digital.kiri.llm.ChatCompletionResponse.Usage

@DslMarker
annotation class ChatCompletionResponseDsl

fun chatCompletionResponse(block: ChatCompletionResponseBuilder.() -> Unit) =
    ChatCompletionResponseBuilder().apply(block).build()

@ChatCompletionResponseDsl
class ChatCompletionResponseBuilder {
    var content: MutableList<ContentItem> = mutableListOf()
    var id: String = ""
    var stopReason: StopReason? = StopReason.END_TURN
    var usage: Usage = Usage(0, 0)

    fun content(block: ChatCompletionResponseContentBuilder.() -> Unit) {
        content.addAll(ChatCompletionResponseContentBuilder().apply(block))
    }

    fun usage(block: ChatCompletionResponseUsageBuilder.() -> Unit) {
        usage = ChatCompletionResponseUsageBuilder().apply(block).build()
    }

    fun build(): ChatCompletionResponse {
        return ChatCompletionResponse(content, id, stopReason, usage)
    }
}

@ChatCompletionResponseDsl
class ChatCompletionResponseContentBuilder: ArrayList<ContentItem>() {
    fun text(text: String) {
        add(ContentItem.Text(text))
    }

    fun toolUse(block: LlmToolUseBuilder.() -> Unit) {
        add(ToolUse(LlmToolUseBuilder().apply(block).build()))
    }
}

@ChatCompletionResponseDsl
class ChatCompletionResponseUsageBuilder {
    var inputTokens: Long = 0
    var outputTokens: Long = 0

    fun build(): Usage {
        return Usage(inputTokens, outputTokens)
    }
}