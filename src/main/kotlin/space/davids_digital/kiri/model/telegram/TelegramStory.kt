package space.davids_digital.kiri.model.telegram

/**
 * [Reference](https://core.telegram.org/bots/api#story)
 */
data class TelegramStory (
    /**
     * Chat that posted the story.
     */
    val chatId: TelegramChatId,
    /**
     * Unique identifier for the story in the chat.
     */
    val id: Long,
)