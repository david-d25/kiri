package space.davids_digital.kiri.model.telegram

/**
 * This object contains information about a paid media purchase.
 */
data class TelegramPaidMediaPurchased (
    /**
     * User who purchased the media
     */
    val user: TelegramUser,

    /**
     * Bot-specified paid media payload
     */
    val paidMediaPayload: String,
)