package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * This object contains basic information about a successful payment. Note that if the buyer initiates a chargeback with
 * the relevant payment provider following this transaction, the funds may be debited from your balance.
 * This is outside of Telegram's control.
 */
data class TelegramSuccessfulPayment (
    /**
     * Three-letter ISO 4217 [currency](https://core.telegram.org/bots/payments#supported-currencies) code, or “XTR” for
     * payments in [Telegram Stars](https://t.me/BotNews/90).
     */
    val currency: String,
    /**
     * Total price in the _smallest units_ of the currency (integer, *not* float/double). For example, for a price of
     * `US$ 1.45` pass `amount = 145`. See the _exp_ parameter in
     * [currencies.json](https://core.telegram.org/bots/payments/currencies.json), it shows the number of digits past
     * the decimal point for each currency (2 for the majority of currencies).
     */
    val totalAmount: Int,
    /**
     * Bot-specified invoice payload.
     */
    val invoicePayload: String,
    /**
     * Expiration date of the subscription; for recurring payments only.
     */
    val subscriptionExpirationDate: ZonedDateTime? = null,
    /**
     * True, if the payment is a recurring payment for a subscription.
     */
    val isRecurring: Boolean = false,
    /**
     * True, if the payment is the first payment for a subscription.
     */
    val isFirstRecurring: Boolean = false,
    /**
     * Identifier of the shipping option chosen by the user.
     */
    val shippingOptionId: String? = null,
    /**
     * Order information provided by the user.
     */
    val orderInfo: TelegramOrderInfo? = null,
    /**
     * 	Telegram payment identifier.
     */
    val telegramPaymentChargeId: String,
    /**
     * Provider payment identifier.
     */
    val providerPaymentChargeId: String,
)