package space.davids_digital.kiri.model.telegram

/**
 * Represents basic information about an invoice.
 */
data class TelegramInvoice (
    /**
     * Product name.
     */
    val title: String,
    /**
     * Product description.
     */
    val description: String,
    /**
     * Unique bot deep-linking parameter that can be used to generate this invoice.
     */
    val startParameter: String,
    /**
     * Three-letter ISO 4217 [currency](https://core.telegram.org/bots/payments#supported-currencies) code,
     * or “XTR” for payments in Telegram Stars.
     */
    val currency: String,
    /**
     * Total price in the _smallest units_ of the currency (integer, *not* float/double).
     * For example, for a price of `US$ 1.45` pass `amount = 145`.
     * See the _exp_ parameter in [currencies.json](https://core.telegram.org/bots/payments/currencies.json), it shows
     * the number of digits past the decimal point for each currency (2 for the majority of currencies).
     */
    val totalAmount: Long,
)