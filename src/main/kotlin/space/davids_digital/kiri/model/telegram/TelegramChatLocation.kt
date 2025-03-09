package space.davids_digital.kiri.model.telegram

/**
 * Represents a location to which a chat is connected.
 */
data class TelegramChatLocation (
    /**
     * The location to which the supergroup is connected. Can't be a live location.
     */
    val location: TelegramLocation,
    /**
     * Location address; 1-64 characters, as defined by the chat owner.
     */
    val address: String,
)