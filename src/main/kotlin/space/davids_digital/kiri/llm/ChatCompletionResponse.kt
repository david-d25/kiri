package space.davids_digital.kiri.llm

data class ChatCompletionResponse (
    val content: List<ContentItem>,
    val id: String,
    val stopReason: StopReason?,
    val usage: Usage,
) {
    sealed interface ContentItem {
        data class Text(val text: String) : ContentItem
        data class ToolUse(val toolUse: ChatCompletionToolUse) : ContentItem
        data class Reasoning(val id: String?, val content: String?, val signature: String) : ContentItem
        data class RedactedReasoning(val data: String) : ContentItem
    }
    enum class StopReason { END_TURN, MAX_TOKENS, STOP_SEQUENCE, TOOL_USE, UNKNOWN }

    /**
     * Normalized token usage across providers.
     *
     * Semantics (must hold for every provider implementation):
     * - [inputTokens] — **fresh / uncached** prompt tokens only. Never includes cache-read or
     *   cache-creation tokens. The total billable prompt prefix is
     *   `inputTokens + cacheReadInputTokens + cacheCreationInputTokens`.
     * - [outputTokens] — completion tokens (including reasoning tokens for providers that surface them).
     * - [cacheReadInputTokens] — tokens served from a prompt cache (discounted price).
     * - [cacheCreationInputTokens] — tokens written to a prompt cache (priced at a premium).
     *   Providers that only do implicit caching (OpenAI, Google) cannot distinguish writes from
     *   fresh input, so this field is always 0 for them.
     *
     * Providers whose native responses expose "total prompt tokens including cache" MUST subtract
     * cached counts before populating [inputTokens] to satisfy this contract.
     *
     * A value of `-1` indicates "unknown" (the provider did not return usage metadata).
     */
    data class Usage(
        val inputTokens: Long,
        val outputTokens: Long,
        val cacheReadInputTokens: Long = 0,
        val cacheCreationInputTokens: Long = 0,
    )
}