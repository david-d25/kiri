package space.davids_digital.kiri.model.telegram

/**
 * Contains information about a user that was shared with the bot using a
 * [KeyboardButtonRequestUsers](https://core.telegram.org/bots/api#keyboardbuttonrequestusers) button.
 */
data class TelegramSharedUser(
    /**
     * Identifier of the shared user. The bot may not have access to the user and could be unable to use this
     * identifier, unless the user is already known to the bot by some other means.
     */
    val userId: TelegramUserId,
    /**
     * First name of the user, if the name was requested by the bot.
     */
    val firstName: String? = null,
    /**
     * Last name of the user, if the name was requested by the bot.
     */
    val lastName: String? = null,
    /**
     * Username of the user, if the username was requested by the bot.
     */
    val username: String? = null,
    /**
     * Available sizes of the chat photo, if the photo was requested by the bot.
     */
    val photo: List<TelegramPhotoSize>? = null,
)
