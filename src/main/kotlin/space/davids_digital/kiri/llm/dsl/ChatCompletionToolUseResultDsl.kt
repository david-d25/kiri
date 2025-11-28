package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.ChatCompletionImageType
import space.davids_digital.kiri.llm.ChatCompletionToolUseResult

@DslMarker
annotation class ChatCompletionToolUseResultDsl

fun chatCompletionToolUseResult(block: ChatCompletionToolUseResultBuilder.() -> Unit) =
    ChatCompletionToolUseResultBuilder().apply(block).build()

@ChatCompletionToolUseResultDsl
class ChatCompletionToolUseResultBuilder {
    var id: String = ""
    var name: String = ""
    var output: ChatCompletionToolUseResult.Output = ChatCompletionToolUseResult.Output.Text("")

    fun output(block: ChatCompletionToolUseResultOutputBuilder.() -> Unit) {
        output = ChatCompletionToolUseResultOutputBuilder().apply(block).build()
    }

    fun build(): ChatCompletionToolUseResult {
        require(id.isNotBlank() || name.isNotBlank()) { "at least one of [id, name] must be set" }
        return ChatCompletionToolUseResult(id, name, output)
    }
}

@ChatCompletionToolUseResultDsl
class ChatCompletionToolUseResultOutputBuilder {
    var text: String = ""
    var image: ByteArray = byteArrayOf()
    var imageType: ChatCompletionImageType = ChatCompletionImageType.PNG

    fun text(text: String) {
        this.text = text
    }

    fun image(data: ByteArray, imageType: ChatCompletionImageType) {
        this.image = data
        this.imageType = imageType
    }

    fun build(): ChatCompletionToolUseResult.Output {
        return when {
            text.isNotBlank() -> ChatCompletionToolUseResult.Output.Text(text)
            image.isNotEmpty() -> ChatCompletionToolUseResult.Output.Image(image, imageType)
            else -> throw IllegalStateException("Either text or image must be set")
        }
    }
}
