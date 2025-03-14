package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.LlmMessageResponse
import space.davids_digital.kiri.llm.LlmMessageResponse.ContentItem
import space.davids_digital.kiri.llm.LlmMessageResponse.ContentItem.ToolUse
import space.davids_digital.kiri.llm.LlmMessageResponse.StopReason
import space.davids_digital.kiri.llm.LlmMessageResponse.Usage

@DslMarker
annotation class LlmMessageResponseDsl

fun llmMessageResponse(block: LlmMessageResponseBuilder.() -> Unit) = LlmMessageResponseBuilder().apply(block).build()

@LlmMessageResponseDsl
class LlmMessageResponseBuilder {
    var content: MutableList<ContentItem> = mutableListOf()
    var id: String = ""
    var stopReason: StopReason? = StopReason.END_TURN
    var usage: Usage = Usage(0, 0)

    fun content(block: LlmMessageResponseContentBuilder.() -> Unit) {
        content.addAll(LlmMessageResponseContentBuilder().apply(block))
    }

    fun usage(block: LlmMessageResponseUsageBuilder.() -> Unit) {
        usage = LlmMessageResponseUsageBuilder().apply(block).build()
    }

    fun build(): LlmMessageResponse {
        return LlmMessageResponse(content, id, stopReason, usage)
    }
}

@LlmMessageResponseDsl
class LlmMessageResponseContentBuilder: ArrayList<ContentItem>() {
    fun text(text: String) {
        add(ContentItem.Text(text))
    }

    fun toolUse(block: LlmToolUseBuilder.() -> Unit) {
        add(ToolUse(LlmToolUseBuilder().apply(block).build()))
    }
}

@LlmMessageResponseDsl
class LlmMessageResponseUsageBuilder {
    var inputTokens: Long = 0
    var outputTokens: Long = 0

    fun build(): Usage {
        return Usage(inputTokens, outputTokens)
    }
}