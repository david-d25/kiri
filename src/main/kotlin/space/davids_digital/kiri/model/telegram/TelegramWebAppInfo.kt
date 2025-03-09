package space.davids_digital.kiri.model.telegram

/**
 * Describes a [Web App](https://core.telegram.org/bots/webapps).
 */
class TelegramWebAppInfo (
    /**
     * An HTTPS URL of a Web App to be opened with additional data as specified in
     * [Initializing Web Apps](https://core.telegram.org/bots/webapps#initializing-mini-apps).
     */
    val url: String,
)