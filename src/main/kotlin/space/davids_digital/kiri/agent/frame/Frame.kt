package space.davids_digital.kiri.agent.frame

data class Frame (
    val type: String,
    val attributes: Map<String, String>,
    val content: String
) {
    class Builder {
        var type: String = ""
        var attributes: Map<String, String> = emptyMap()
        var content: String = ""

        fun build(): Frame {
            return Frame(type, attributes, content)
        }
    }
}