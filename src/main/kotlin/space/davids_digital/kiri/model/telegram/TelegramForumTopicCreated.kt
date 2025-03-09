package space.davids_digital.kiri.model.telegram

import java.awt.Color

/**
 * This object represents a service message about a new forum topic created in the chat.
 */
class TelegramForumTopicCreated (
    val name: String,
    val iconColor: Color,
    /**
     * Unique identifier of the custom emoji shown as the topic icon.
     */
    val iconCustomEmojiId: String? = null,
)