package space.davids_digital.kiri.model.telegram

/**
 * Describes Telegram Passport data shared with the bot by the user.
 */
data class TelegramPassportData (
    /**
     * Array with information about documents and other Telegram Passport elements that was shared with the bot.
     */
    val data: List<TelegramEncryptedPassportElement>,
    /**
     * Encrypted credentials required to decrypt the data.
     */
    val credentials: TelegramEncryptedCredentials,
)