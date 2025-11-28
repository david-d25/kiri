package space.davids_digital.kiri.agent.frame.dsl

import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.llm.ChatCompletionImageType

@DslMarker
annotation class DataFrameDsl

fun dataFrameContent(block: FrameContentBuilder.() -> Unit): List<DataFrame.ContentPart> {
    val builder = FrameContentBuilder()
    builder.block()
    return optimize(builder.build())
}

private fun optimize(list: List<DataFrame.ContentPart>): List<DataFrame.ContentPart> {
    val result = ArrayList<DataFrame.ContentPart>(list.size)
    val latestPartTextBuffer = StringBuilder()
    for (part in list) {
        when (part) {
            is DataFrame.Text -> latestPartTextBuffer.append(part.text)
            is DataFrame.Image -> {
                if (latestPartTextBuffer.isNotEmpty()) {
                    result.add(DataFrame.Text(latestPartTextBuffer.toString()))
                    latestPartTextBuffer.clear()
                }
                result.add(part)
            }
        }
    }
    if (latestPartTextBuffer.isNotEmpty()) {
        result.add(DataFrame.Text(latestPartTextBuffer.toString()))
    }
    return result
}

@DataFrameDsl
class FrameContentBuilder {
    private val parts = mutableListOf<DataFrame.ContentPart>()

    fun text(text: String) {
        parts.add(DataFrame.Text(text))
    }

    fun line(text: String) {
        parts.add(DataFrame.Text(text + "\n"))
    }

    fun image(data: ByteArray, type: ChatCompletionImageType) {
        parts.add(DataFrame.Image(data, type))
    }

    fun build(): List<DataFrame.ContentPart> = parts
}