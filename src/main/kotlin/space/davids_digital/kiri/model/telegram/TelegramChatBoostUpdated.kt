package space.davids_digital.kiri.model.telegram

/**
 * This object represents a boost added to a chat or changed.
 */
data class TelegramChatBoostUpdated(
    /**
     * Chat which was boosted
     */
    val chat: TelegramChat,

    /**
     * Information about the chat boost
     */
    val boost: TelegramChatBoost? = null,
)
