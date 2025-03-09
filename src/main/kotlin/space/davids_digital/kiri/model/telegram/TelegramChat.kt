package space.davids_digital.kiri.model.telegram

typealias TelegramChatId = Long

data class TelegramChat (
    val id: TelegramChatId,
    val type: Type,
    /**
     * Title, for supergroups, channels and group chats
     */
    val title: String? = null,
    /**
     * Username, for private chats, supergroups and channels if available
     */
    val username: String? = null,
    /**
     * First name of the other party in a private chat
     */
    val firstName: String? = null,
    /**
     * Last name of the other party in a private chat
     */
    val lastName: String? = null,
    /**
     * Chat photo.
     */
    val photo: TelegramChatPhoto? = null,
    /**
     * Bio of the other party in a private chat
     */
    val bio: String? = null,
    /**
     * Description, for groups, supergroups and channel chats
     */
    val description: String? = null,
    /**
     * Primary invite link, for groups, supergroups and channel chats
     */
    val inviteLink: String? = null,
    /**
     * The most recent pinned message (by sending date)
     */
    val pinnedMessage: TelegramMessage? = null,
    /**
     * Default chat member permissions, for groups and supergroups.
     */
    val permissions: TelegramChatPermissions? = null,
    /**
     * For supergroups, the minimum allowed delay between consecutive messages sent by each unprivileged user;
     * in seconds.
     */
    val slowModeDelay: Int? = null,
    /**
     * For supergroups, name of the group sticker set.
     */
    val stickerSetName: String? = null,
    /**
     * True, if the bot can change the group sticker set.
     */
    val canSetStickerSet: Boolean? = null,
    /**
     * Unique identifier for the linked chat, i.e. the discussion group identifier for a channel and vice versa; for
     * supergroups and channel chats.
     */
    val linkedChatId: Long? = null,
    /**
     * For supergroups, the location to which the supergroup is connected.
     */
    val location: TelegramChatLocation? = null,
) {
    enum class Type {
        PRIVATE,
        GROUP,
        SUPERGROUP,
        CHANNEL,
        UNKNOWN
    }
}