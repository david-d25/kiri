package space.davids_digital.kiri.agent.frame

data class StaticDataFrame(val tag: String, val attributes: Map<String, String>, val content: String) : Frame() {
    class Builder {
        var tag: String = ""
        var attributes: Map<String, String> = emptyMap()
        var content: String = ""
        fun build() = StaticDataFrame(tag, attributes, content)
    }
}