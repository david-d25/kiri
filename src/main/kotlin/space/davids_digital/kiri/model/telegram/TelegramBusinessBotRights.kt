package space.davids_digital.kiri.model.telegram

/**
 * Represents the rights of a business bot.
 */
data class TelegramBusinessBotRights(
    /**
     * True, if the bot can send and edit messages in the private chats that had incoming messages in the last 24 hours
     */
    val canReply: Boolean = false,

    /**
     * True, if the bot can mark incoming private messages as read
     */
    val canReadMessages: Boolean = false,

    /**
     * True, if the bot can delete messages sent by the bot
     */
    val canDeleteSentMessages: Boolean = false,

    /**
     * True, if the bot can delete all private messages in managed chats
     */
    val canDeleteAllMessages: Boolean = false,

    /**
     * True, if the bot can edit the first and last name of the business account
     */
    val canEditName: Boolean = false,

    /**
     * True, if the bot can edit the bio of the business account
     */
    val canEditBio: Boolean = false,

    /**
     * True, if the bot can edit the profile photo of the business account
     */
    val canEditProfilePhoto: Boolean = false,

    /**
     * True, if the bot can edit the username of the business account
     */
    val canEditUsername: Boolean = false,

    /**
     *  True, if the bot can change the privacy settings pertaining to gifts for the business account
     */
    val canChangeGiftSettings: Boolean = false,

    /**
     * True, if the bot can view gifts and the amount of Telegram Stars owned by the business account
     */
    val canViewGiftsAndStars: Boolean = false,

    /**
     * True, if the bot can convert regular gifts owned by the business account to Telegram Stars
     */
    val canConvertGiftsToStars: Boolean = false,

    /**
     * True, if the bot can transfer and upgrade gifts owned by the business account
     */
    val canTransferAndUpgradeGifts: Boolean = false,

    /**
     * True, if the bot can transfer Telegram Stars received by the business account to its own account, or use them to
     * upgrade and transfer gifts
     */
    val canTransferStars: Boolean = false,

    /**
     * True, if the bot can post, edit and delete stories on behalf of the business account
     */
    val canManageStories: Boolean = false,
)
