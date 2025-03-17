package space.davids_digital.kiri.model.telegram

/**
 * This object represents a service message about a user allowing a bot to write messages after adding it to the
 * attachment menu, launching a Web App from a link, or accepting an explicit request from a Web App sent by the method
 * [requestWriteAccess](https://core.telegram.org/bots/webapps#initializing-mini-apps).
 *
 * When the `requestWriteAccess` method becomes available in the Kiri codebase as a service, please replace the API
 * link in this KDoc with the corresponding service call.
 */
data class TelegramWriteAccessAllowed(
    /**
     * True, if the access was granted after the user accepted an explicit request from a Web App sent by the method
     * [requestWriteAccess](https://core.telegram.org/bots/webapps#initializing-mini-apps).
     */
    val fromRequest: Boolean = false,
    /**
     * Name of the Web App, if the access was granted when the Web App was launched from a link.
     */
    val webAppName: String? = null,
    /**
     * True, if the access was granted when the bot was added to the attachment or side menu.
     */
    val fromAttachmentMenu: Boolean = false,
)