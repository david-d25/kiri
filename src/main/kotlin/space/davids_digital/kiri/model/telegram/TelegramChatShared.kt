package space.davids_digital.kiri.model.telegram

/**
 * This object contains information about a chat that was shared with the bot using a
 * [KeyboardButtonRequestChat](https://core.telegram.org/bots/api#keyboardbuttonrequestchat) button.
 */
data class TelegramChatShared(
    val requestId: Long,
    /**
     * Identifier of the shared chat. The bot may not have access to the chat and could be unable to use this
     * identifier, unless the chat is already known to the bot by some other means.
     */
    val chatId: Long,
    /**
     * Title of the chat, if the title was requested by the bot.
     */
    val title: String? = null,
    /**
     * Username of the chat, if the username was requested by the bot and available.
     */
    val username: String? = null,
    /**
     * Available sizes of the chat photo, if the photo was requested by the bot.
     */
    val photo: List<TelegramPhotoSize>? = null,
)
