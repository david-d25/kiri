package space.davids_digital.kiri.model.telegram

/**
 * This object represents the content of a service message, sent whenever a user in the chat triggers a proximity alert
 * set by another user.
 */
data class TelegramProximityAlertTriggered(
    /**
     * User that triggered the alert.
     */
    val traveler: TelegramUserId,
    /**
     * User that set the alert.
     */
    val watcher: TelegramUserId,
    /**
     * The distance between the users.
     */
    val distance: Int,
)
