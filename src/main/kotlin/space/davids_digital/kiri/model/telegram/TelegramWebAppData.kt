package space.davids_digital.kiri.model.telegram

/**
 * Describes data sent from a [Web App](https://core.telegram.org/bots/webapps) to the bot.
 */
data class TelegramWebAppData (
    /**
     * The data. Be aware that a bad client can send arbitrary data in this field.
     */
    val data: String,
    /**
     * Text of the web_app keyboard button from which the Web App was opened.
     * Be aware that a bad client can send arbitrary data in this field.
     */
    val buttonText: String,
)