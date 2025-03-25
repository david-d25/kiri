package space.davids_digital.kiri.agent.frame

data class StaticDataFrame(
    override val tag: String,
    override val attributes: Map<String, String>,
    private val content: List<ContentPart>
) : DataFrame() {
    override fun renderContent() = content

    class Builder {
        var tag: String = ""
        var attributes: MutableMap<String, String> = mutableMapOf()
        var content: List<ContentPart> = emptyList()
        fun build() = StaticDataFrame(tag, attributes, content)
    }
}