package space.davids_digital.kiri.agent.frame.dsl

import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.llm.LlmImageType

@DslMarker
annotation class DataFrameDsl

fun dataFrameContent(block: FrameContentBuilder.() -> Unit): List<DataFrame.ContentPart> {
    val builder = FrameContentBuilder()
    builder.block()
    return builder.build()
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

    fun image(data: ByteArray, type: LlmImageType) {
        parts.add(DataFrame.Image(data, type))
    }

    fun build(): List<DataFrame.ContentPart> = parts
}