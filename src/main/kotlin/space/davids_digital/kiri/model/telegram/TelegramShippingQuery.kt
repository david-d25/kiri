package space.davids_digital.kiri.model.telegram

/**
 * This object contains information about an incoming shipping query.
 */
data class TelegramShippingQuery (
    /**
     * Unique query identifier
     */
    val id: String,

    /**
     * User who sent the query
     */
    val from: TelegramUser,

    /**
     * Bot-specified invoice payload
     */
    val invoicePayload: String,

    /**
     * User specified shipping address
     */
    val shippingAddress: TelegramShippingAddress? = null
)
