package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.LlmImageType
import space.davids_digital.kiri.llm.LlmToolUseResult

@DslMarker
annotation class LlmToolUseResultDsl

fun llmToolUseResult(block: LlmToolUseResultBuilder.() -> Unit) = LlmToolUseResultBuilder().apply(block).build()

@LlmToolUseResultDsl
class LlmToolUseResultBuilder {
    var id: String = ""
    var output: LlmToolUseResult.Output = LlmToolUseResult.Output.Text("")

    fun output(block: LlmToolUseResultOutputBuilder.() -> Unit) {
        output = LlmToolUseResultOutputBuilder().apply(block).build()
    }

    fun build(): LlmToolUseResult {
        require(id.isNotBlank()) { "id must be set" }
        return LlmToolUseResult(id, output)
    }
}

@LlmToolUseResultDsl
class LlmToolUseResultOutputBuilder {
    var text: String = ""
    var image: ByteArray = byteArrayOf()
    var imageType: LlmImageType = LlmImageType.PNG

    fun text(text: String) {
        this.text = text
    }

    fun image(data: ByteArray, imageType: LlmImageType) {
        this.image = data
        this.imageType = imageType
    }

    fun build(): LlmToolUseResult.Output {
        return when {
            text.isNotBlank() -> LlmToolUseResult.Output.Text(text)
            image.isNotEmpty() -> LlmToolUseResult.Output.Image(image, imageType)
            else -> throw IllegalStateException("Either text or image must be set")
        }
    }
}
