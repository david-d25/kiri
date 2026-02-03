package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.ChatCompletionResponse
import space.davids_digital.kiri.llm.ChatCompletionResponse.ContentItem
import space.davids_digital.kiri.llm.ChatCompletionResponse.ContentItem.ToolUse
import space.davids_digital.kiri.llm.ChatCompletionResponse.StopReason
import space.davids_digital.kiri.llm.ChatCompletionResponse.Usage
import space.davids_digital.kiri.llm.ChatCompletionWebSearch

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

    fun toolUse(block: ChatCompletionToolUseBuilder.() -> Unit) {
        add(ToolUse(ChatCompletionToolUseBuilder().apply(block).build()))
    }

    fun reasoning(block: ChatCompletionReasoningBuilder.() -> Unit) {
        val reasoning = ChatCompletionReasoningBuilder().apply(block).build()
        add(ContentItem.Reasoning(reasoning.id, reasoning.content, reasoning.signature))
    }

    fun redactedReasoning(data: String) {
        add(ContentItem.RedactedReasoning(data))
    }

    fun webSearchOpenPage(block: WebSearchOpenPageBuilder.() -> Unit) {
        val builder = WebSearchOpenPageBuilder().apply(block)
        add(ChatCompletionWebSearch.OpenPage(builder.id, builder.url, builder.content))
    }

    fun webSearchFindInPage(id: String?, pattern: String, url: String) {
        add(ChatCompletionWebSearch.FindInPage(id, pattern, url))
    }

    fun webSearchSearch(id: String?, query: String, sourceUrls: List<String>) {
        add(ChatCompletionWebSearch.Search(id, query, sourceUrls))
    }
}

@ChatCompletionResponseDsl
class WebSearchOpenPageBuilder {
    var id: String? = null
    var url: String = ""
    var content: MutableList<ChatCompletionWebSearch.OpenPage.Content> = mutableListOf()

    fun content(url: String, title: String, encryptedContent: String, pageAge: String?) {
        content.add(ChatCompletionWebSearch.OpenPage.Content(url, title, encryptedContent, pageAge))
    }
}


@ChatCompletionResponseDsl
class ChatCompletionReasoningBuilder {
    var id: String? = null
    var content: String? = null
    var signature: String = ""

    fun build(): ContentItem.Reasoning {
        return ContentItem.Reasoning(id, content, signature)
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