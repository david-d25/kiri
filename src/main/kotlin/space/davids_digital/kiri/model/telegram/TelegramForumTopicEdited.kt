package space.davids_digital.kiri.model.telegram

data class TelegramForumTopicEdited (
    /**
     * New name of the topic, if it was edited.
     */
    val name: String? = null,
    /**
     * New identifier of the custom emoji shown as the topic icon, if it was edited; an empty string if the icon was
     * removed.
     */
    val iconCustomEmojiId: String? = null,
)