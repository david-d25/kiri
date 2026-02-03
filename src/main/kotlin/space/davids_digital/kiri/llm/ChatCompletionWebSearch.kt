package space.davids_digital.kiri.llm

sealed class ChatCompletionWebSearch : ChatCompletionRequest.Message.ContentItem, ChatCompletionResponse.ContentItem {
    data class OpenPage(val id: String?, val url: String, val content: List<Content>) : ChatCompletionWebSearch() {
        data class Content(
            val url: String,
            val title: String,
            val encryptedContent: String,
            val pageAge: String?
        )
    }
    data class FindInPage(val id: String?, val pattern: String, val url: String) : ChatCompletionWebSearch()
    data class Search(val id: String?, val query: String, val sourceUrls: List<String>) : ChatCompletionWebSearch()
}