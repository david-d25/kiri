package space.davids_digital.kiri.agent.frame

data class Frame (
    val type: String,
    val attributes: Map<String, String>,
    val content: String
)