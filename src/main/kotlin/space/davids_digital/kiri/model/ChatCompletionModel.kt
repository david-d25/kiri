package space.davids_digital.kiri.model

data class ChatCompletionModel (
    val handle: String,
    val features: Features,
    val reasoningType: ReasoningType = ReasoningType.NONE
) {
    data class Features(
        val webSearch: Boolean = false,
        val reasoningEffort: Boolean = false,
        val reasoningMaxTokens: Boolean = false,
    )

    enum class ReasoningType {
        NONE, OPTIONAL, REQUIRED
    }
}