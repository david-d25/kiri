package space.davids_digital.kiri.model.telegram

/**
 * Describes documents or other Telegram Passport elements shared with the bot by the user.
 */
data class TelegramEncryptedPassportElement(
    val type: Type,
    /**
     * Base64-encoded encrypted Telegram Passport element data provided by the user; available only for
     * [Type.PERSONAL_DETAILS], [Type.PASSPORT], [Type.DRIVER_LICENSE], [Type.IDENTITY_CARD], [Type.INTERNAL_PASSPORT]
     * and [Type.ADDRESS] types. Can be decrypted and verified using the accompanying [TelegramEncryptedCredentials].
     */
    val dataBase64: String? = null,
    /**
     * User's verified phone number; available only for [Type.PHONE_NUMBER] type.
     */
    val phoneNumber: String? = null,
    /**
     * User's verified email address; available only for [Type.EMAIL] type
     */
    val email: String? = null,
    /**
     * Array of encrypted files with documents provided by the user; available only for [Type.UTILITY_BILL],
     * [Type.BANK_STATEMENT], [Type.RENTAL_AGREEMENT], [Type.PASSPORT_REGISTRATION] and [Type.TEMPORARY_REGISTRATION]
     * types. Files can be decrypted and verified using the accompanying [TelegramEncryptedCredentials].
     */
    val files: List<TelegramPassportFile> = emptyList(),
    /**
     * Encrypted file with the front side of the document, provided by the user; available only for [Type.PASSPORT],
     * [Type.DRIVER_LICENSE], [Type.IDENTITY_CARD] and [Type.INTERNAL_PASSPORT].
     * The file can be decrypted and verified using the accompanying [TelegramEncryptedCredentials].
     */
    val frontSide: TelegramPassportFile? = null,
    /**
     * Encrypted file with the reverse side of the document, provided by the user; available only for
     * [Type.DRIVER_LICENSE] and [Type.IDENTITY_CARD].
     * The file can be decrypted and verified using the accompanying [TelegramEncryptedCredentials].
     */
    val reverseSide: TelegramPassportFile? = null,
    /**
     * Encrypted file with the selfie of the user holding a document, provided by the user; available if requested for
     * [Type.PASSPORT], [Type.DRIVER_LICENSE], [Type.IDENTITY_CARD] and [Type.INTERNAL_PASSPORT].
     * The file can be decrypted and verified using the accompanying [TelegramEncryptedCredentials].
     */
    val selfie: TelegramPassportFile? = null,
    /**
     * Array of encrypted files with translated versions of documents provided by the user; available if requested for
     * [Type.PASSPORT], [Type.DRIVER_LICENSE], [Type.IDENTITY_CARD], [Type.INTERNAL_PASSPORT], [Type.UTILITY_BILL],
     * [Type.BANK_STATEMENT], [Type.RENTAL_AGREEMENT], [Type.PASSPORT_REGISTRATION]
     * and [Type.TEMPORARY_REGISTRATION] types.
     * Files can be decrypted and verified using the accompanying [TelegramEncryptedCredentials].
     */
    val translation: List<TelegramPassportFile> = emptyList(),
    /**
     * Element hash for using in [TelegramPassportElementErrorUnspecified].
     */
    val hashBase64: String? = null,
) {
    enum class Type {
        PERSONAL_DETAILS,
        PASSPORT,
        DRIVER_LICENSE,
        IDENTITY_CARD,
        INTERNAL_PASSPORT,
        ADDRESS,
        UTILITY_BILL,
        BANK_STATEMENT,
        RENTAL_AGREEMENT,
        PASSPORT_REGISTRATION,
        TEMPORARY_REGISTRATION,
        PHONE_NUMBER,
        EMAIL,
    }
}