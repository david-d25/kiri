package space.davids_digital.kiri.model.telegram

/**
 * This object represents an inline button that switches the current user to inline mode in a chosen chat, with an
 * optional default inline query.
 */
data class TelegramSwitchInlineQueryChosenChat (
    /**
     * The default inline query to be inserted in the input field.
     * If left empty, only the bot's username will be inserted.
     */
    val query: String? = null,
    /**
     * True, if private chats with users can be chosen.
     */
    val allowUserChats: Boolean? = null,
    /**
     * True, if private chats with bots can be chosen.
     */
    val allowBotChats: Boolean? = null,
    /**
     * True, if group and supergroup chats can be chosen.
     */
    val allowGroupChats: Boolean? = null,
    /**
     * True, if channel chats can be chosen.
     */
    val allowChannelChats: Boolean? = null,
)