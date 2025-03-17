package space.davids_digital.kiri.model.telegram

typealias TelegramUserId = Long

/**
 * Represents a Telegram user or bot.
 */
data class TelegramUser (
    /**
     * Unique identifier for this user or bot.
     */
    val id: TelegramUserId,
    /**
     * True, if this user is a bot.
     */
    val isBot: Boolean,
    /**
     * User's or bot's first name.
     */
    val firstName: String,
    /**
     * User's or bot's last name.
     */
    val lastName: String? = null,
    /**
     * User's or bot's username.
     */
    val username: String? = null,
    /**
     * IETF language tag of the user's language.
     */
    val languageCode: String? = null,
    /**
     * True, if this user is a Telegram Premium user.
     */
    val isPremium: Boolean = false,
    /**
     * True, if this user added the bot to the attachment menu.
     */
    val addedToAttachmentMenu: Boolean = false,
    /**
     * True, if the bot can be invited to groups. Returned only in [getMe](https://core.telegram.org/bots/api#getme).
     */
    val canJoinGroups: Boolean = false,
    /**
     * True, if [privacy mode](https://core.telegram.org/bots/features#privacy-mode) is disabled for the bot.
     * Returned only in [getMe](https://core.telegram.org/bots/api#getme).
     */
    val canReadAllGroupMessages: Boolean = false,
    /**
     * True, if the bot supports inline queries. Returned only in [getMe](https://core.telegram.org/bots/api#getme).
     */
    val supportsInlineQueries: Boolean = false,
    /**
     * True, if the bot can be connected to a Telegram Business account to receive its messages.
     * Returned only in [getMe](https://core.telegram.org/bots/api#getme).
     */
    val canConnectToBusiness: Boolean = false,
    /**
     * True, if the bot has a main Web App. Returned only in [getMe](https://core.telegram.org/bots/api#getme).
     */
    val hasMainWebApp: Boolean = false,
)