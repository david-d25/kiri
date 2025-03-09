package space.davids_digital.kiri.model.telegram

data class TelegramOrderInfo(
    /**
     * User name.
     */
    val name: String? = null,
    /**
     * User's phone number.
     */
    val phoneNumber: String? = null,
    /**
     * User email.
     */
    val email: String? = null,
    /**
     * User shipping address.
     */
    val shippingAddress: TelegramShippingAddress? = null,
)
