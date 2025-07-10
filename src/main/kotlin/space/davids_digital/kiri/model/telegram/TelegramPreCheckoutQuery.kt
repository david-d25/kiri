package space.davids_digital.kiri.model.telegram

/**
 * This object contains information about an incoming pre-checkout query.
 */
data class TelegramPreCheckoutQuery (
    /**
     * Unique query identifier
     */
    val id: String,

    /**
     * User who sent the query
     */
    val from: TelegramUser,

    /**
     * Three-letter ISO 4217 [currency](https://core.telegram.org/bots/payments#supported-currencies) code, or “XTR”
     * for payments in [Telegram Stars](https://t.me/BotNews/90)
     */
    val currency: String,

    /**
     * 	Total price in the _smallest units_ of the currency (integer, **not** float/double).
     * 	For example, for a price of `US$ 1.45` pass `amount = 145`. See the _exp_ parameter in
     * 	[currencies.json](https://core.telegram.org/bots/payments/currencies.json), it shows the number of digits past
     * 	the decimal point for each currency (2 for the majority of currencies).
     */
    val totalAmount: Int,

    /**
     * Bot-specified invoice payload
     */
    val invoicePayload: String,

    /**
     * Identifier of the shipping option chosen by the user
     */
    val shippingOptionId: String? = null,

    /**
     * Order information provided by the user
     */
    val orderInfo: TelegramOrderInfo? = null
)