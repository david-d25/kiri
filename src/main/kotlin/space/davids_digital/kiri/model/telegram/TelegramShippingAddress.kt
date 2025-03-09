package space.davids_digital.kiri.model.telegram

data class TelegramShippingAddress(
    /**
     * 	Two-letter [ISO 3166-1 alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) country code.
     */
    val countryCode: String,
    /**
     * State, if applicable.
     */
    val state: String,
    val city: String,
    val streetLine1: String,
    val streetLine2: String,
    val postcode: String,
)
