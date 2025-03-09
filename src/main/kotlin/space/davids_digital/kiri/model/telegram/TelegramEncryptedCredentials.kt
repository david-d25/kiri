package space.davids_digital.kiri.model.telegram

/**
 * Describes data required for decrypting and authenticating [TelegramEncryptedPassportElement].
 * See the [Telegram Passport Documentation](https://core.telegram.org/passport#receiving-information) for a complete
 * description of the data decryption and authentication processes.
 */
data class TelegramEncryptedCredentials(
    /**
     * Base64-encoded encrypted JSON-serialized data with unique user's payload, data hashes and secrets required for
     * [TelegramEncryptedPassportElement] decryption and authentication.
     */
    val dataBase64: String,
    /**
     * Base64-encoded data hash for data authentication.
     */
    val hashBase64: String,
    /**
     * Base64-encoded secret, encrypted with the bot's public RSA key, required for data decryption.
     */
    val secretBase64: String,
)
