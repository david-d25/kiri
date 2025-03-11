package space.davids_digital.kiri.llm

import space.davids_digital.kiri.llm.LlmMessageResponse.ContentItem
import space.davids_digital.kiri.llm.LlmMessageResponse.ContentItem.ToolUse
import space.davids_digital.kiri.llm.LlmMessageResponse.ContentItem.ToolUse.Input
import space.davids_digital.kiri.llm.LlmMessageResponse.StopReason
import space.davids_digital.kiri.llm.LlmMessageResponse.Usage

@DslMarker
annotation class LlmMessageResponseDsl

fun llmMessageResponse(block: LlmMessageResponseBuilder.() -> Unit) = LlmMessageResponseBuilder().apply(block).build()
fun toolUseInput(block: LlmMessageResponseToolUseInputBuilder.() -> Unit) =
    LlmMessageResponseToolUseInputBuilder().apply(block).build()

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

    fun toolUse(block: LlmMessageResponseToolUseBuilder.() -> Unit) {
        add(LlmMessageResponseToolUseBuilder().apply(block).build())
    }
}

@LlmMessageResponseDsl
class LlmMessageResponseToolUseBuilder {
    var id: String = ""
    var name: String = ""
    var input: Input = Input.ObjectValue(emptyMap())

    fun build(): ToolUse {
        return ToolUse(id, name, input)
    }
}

@LlmMessageResponseDsl
class LlmMessageResponseToolUseInputBuilder {
    private var value: Input = Input.ObjectValue(emptyMap())

    fun boolean(boolean: Boolean) {
        value = Input.BooleanValue(boolean)
    }

    fun number(number: Double) {
        value = Input.NumberValue(number)
    }

    fun text(text: String) {
        value = Input.TextValue(text)
    }

    fun array(block: MutableList<Input>.() -> Unit) {
        value = Input.ArrayValue(ArrayList<Input>().apply(block))
    }

    fun `object`(block: MutableMap<String, Input>.() -> Unit) {
        value = Input.ObjectValue(HashMap<String, Input>().apply(block))
    }

    fun build(): Input {
        return value
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