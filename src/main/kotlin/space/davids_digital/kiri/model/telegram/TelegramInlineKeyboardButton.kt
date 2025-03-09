package space.davids_digital.kiri.model.telegram

/**
 * Represents one button of an inline keyboard. Exactly one of the optional fields must be used to specify type of the
 * button.
 */
data class TelegramInlineKeyboardButton (
    /**
     * Label text on the button.
     */
    val text: String,
    /**
     * HTTP or tg:// URL to be opened when the button is pressed. Links `tg://user?id=<user_id>` can be used to mention
     * a user by their identifier without using a username, if this is allowed by their privacy settings.
     */
    val url: String? = null,
    /**
     * Data to be sent in a [callback query](https://core.telegram.org/bots/api#callbackquery) to the bot when the
     * button is pressed, 1-64 bytes.
     */
    val callbackData: String? = null,
    /**
     * Description of the [Web App](https://core.telegram.org/bots/webapps) that will be launched when the user presses
     * the button. The Web App will be able to send an arbitrary message on behalf of the user using the method
     * [answerWebAppQuery](https://core.telegram.org/bots/api#answerwebappquery). Available only in private chats
     * between a user and the bot. Not supported for messages sent on behalf of a Telegram Business account.
     */
    val webbApp: TelegramWebAppInfo? = null,
    /**
     * An HTTPS URL used to automatically authorize the user. Can be used as a replacement for the
     * [Telegram Login Widget](https://core.telegram.org/widgets/login).
     */
    val loginUrl: TelegramLoginUrl? = null,
    /**
     * If set, pressing the button will prompt the user to select one of their chats, open that chat and insert the
     * bot's username and the specified inline query in the input field. May be empty, in which case just the bot's
     * username will be inserted. Not supported for messages sent on behalf of a Telegram Business account.
     */
    val switchInlineQuery: String? = null,
    /**
     * If set, pressing the button will insert the bot's username and the specified inline query in the current chat's
     * input field. May be empty, in which case only the bot's username will be inserted.
     *
     * This offers a quick way for the user to open your bot in inline mode in the same chat - good for selecting
     * something from multiple options. Not supported in channels and for messages sent on behalf of a Telegram Business
     * account.
     */
    val switchInlineQueryCurrentChat: String? = null,
    /**
     * If set, pressing the button will prompt the user to select one of their chats of the specified type, open that
     * chat and insert the bot's username and the specified inline query in the input field. Not supported for messages
     * sent on behalf of a Telegram Business account.
     */
    val switchInlineQueryChosenChat: TelegramSwitchInlineQueryChosenChat? = null,
    /**
     * Description of the button that copies the specified text to the clipboard.
     */
    val copyText: TelegramCopyTextButton? = null,
    /**
     * Description of the game that will be launched when the user presses the button.
     *
     * **NOTE:** This type of button **must** always be the first button in the first row.
     */
    val callbackGame: TelegramCallbackGame? = null,
    /**
     * Specify True, to send a [Pay button](https://core.telegram.org/bots/api#payments). Substrings “⭐” and “XTR” in
     * the button's text will be replaced with a Telegram Star icon.
     *
     * NOTE: This type of button must always be the first button in the first row and can only be used in invoice
     * messages.
     */
    val pay: Boolean? = null,
)