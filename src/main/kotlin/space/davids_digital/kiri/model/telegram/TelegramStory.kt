package space.davids_digital.kiri.model.telegram

data class TelegramStory (
    /**
     * Chat that posted the story.
     */
    val chatId: Long,
    /**
     * Unique identifier for the story in the chat.
     */
    val id: Long,
)