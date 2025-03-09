package space.davids_digital.kiri.model.telegram

/**
 * Contains information about the users whose identifiers were shared with the bot using a
 * [KeyboardButtonRequestUsers](https://core.telegram.org/bots/api#keyboardbuttonrequestusers) button.
 */
data class TelegramUsersShared(
    val requestId: Long,
    /**
     * Information about users shared with the bot.
     */
    val users: List<TelegramSharedUser>,
)
