package space.davids_digital.kiri.rest.dto

data class ChatCompletionModelDto (
    val handle: String,
    val features: Features,
    val reasoningType: ReasoningType = ReasoningType.NONE
) {
    data class Features(
        val webSearch: Boolean,
        val reasoningEffort: Boolean,
        val reasoningMaxTokens: Boolean
    )

    enum class ReasoningType {
        NONE, OPTIONAL, REQUIRED
    }
}
