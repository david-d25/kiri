package space.davids_digital.kiri.model.telegram

/**
 * Represents a service message about a change in auto-delete timer settings.
 */
data class TelegramMessageAutoDeleteTimerChanged (
    /**
     * New auto-delete time for messages in the chat; in seconds.
     */
    val messageAutoDeleteTime: Int,
)