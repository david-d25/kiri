package space.davids_digital.kiri.model.telegram

/**
 * Represents a phone contact.
 */
data class TelegramContact(
    /**
     * Contact's phone number.
     */
    val phoneNumber: String,
    /**
     * Contact's first name.
     */
    val firstName: String,
    /**
     * Contact's last name.
     */
    val lastName: String? = null,
    /**
     * Contact's user identifier in Telegram.
     */
    val userId: TelegramUserId? = null,
    /**
     * Additional data about the contact in the form of a [vCard](https://en.wikipedia.org/wiki/VCard).
     */
    val vCard: String? = null
)
