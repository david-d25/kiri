package space.davids_digital.kiri.agent.frame

import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent

data class StaticDataFrame(
    override val tag: String,
    override val attributes: Map<String, String>,
    private val content: String
) : DataFrame() {
    override fun renderContent() = dataFrameContent {
        text(content)
    }

    class Builder {
        var tag: String = ""
        var attributes: Map<String, String> = emptyMap()
        var content: String = ""
        fun build() = StaticDataFrame(tag, attributes, content)
    }
}