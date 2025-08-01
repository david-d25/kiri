package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * This object contains information about one member of a chat.
 */
sealed class TelegramChatMember(
    /**
     * 	The member's status in the chat
     */
    val status: String,

    /**
     * Information about the user
     */
    open val user: TelegramUser,
) {
    /**
     * Represents a chat member that owns the chat and has all administrator privileges.
     */
    data class Owner(
        override val user: TelegramUser,

        /**
         * True, if the user's presence in the chat is hidden
         */
        val isAnonymous: Boolean = false,

        /**
         * Custom title for this user
         */
        val customTitle: String? = null,
    ): TelegramChatMember("creator", user)

    /**
     * Represents a chat member that has some additional privileges.
     */
    data class Administrator(
        override val user: TelegramUser,

        /**
         * True, if the bot is allowed to edit administrator privileges of that user
         */
        val canBeEdited: Boolean = false,

        /**
         * True, if the user's presence in the chat is hidden
         */
        val isAnonymous: Boolean = false,

        /**
         * True, if the administrator can access the chat event log, get boost list, see hidden supergroup and channel
         * members, report spam messages and ignore slow mode. Implied by any other administrator privilege.
         */
        val canManageChat: Boolean = false,

        /**
         * True, if the administrator can delete messages of other users
         */
        val canDeleteMessages: Boolean = false,

        /**
         * True, if the administrator can manage video chats
         */
        val canManageVideoChats: Boolean = false,

        /**
         * True, if the administrator can restrict, ban or unban chat members, or access supergroup statistics
         */
        val canRestrictMembers: Boolean = false,

        /**
         * True, if the administrator can add new administrators with a subset of their own privileges or demote
         * administrators that they have promoted, directly or indirectly (promoted by administrators that were
         * appointed by the user)
         */
        val canPromoteMembers: Boolean = false,

        /**
         * True, if the user is allowed to change the chat title, photo and other settings
         */
        val canChangeInfo: Boolean = false,

        /**
         * True, if the user is allowed to invite new users to the chat
         */
        val canInviteUsers: Boolean = false,

        /**
         * True, if the administrator can post stories to the chat
         */
        val canPostStories: Boolean = false,

        /**
         * True, if the administrator can edit stories posted by other users, post stories to the chat page, pin chat
         * stories, and access the chat's story archive
         */
        val canEditStories: Boolean = false,

        /**
         * True, if the administrator can delete stories posted by other users
         */
        val canDeleteStories: Boolean = false,

        /**
         * True, if the administrator can post messages in the channel, or access channel statistics; for channels only
         */
        val canPostMessages: Boolean = false,

        /**
         * True, if the administrator can edit messages of other users and can pin messages; for channels only
         */
        val canEditMessages: Boolean = false,

        /**
         * True, if the user is allowed to pin messages; for groups and supergroups only
         */
        val canPinMessages: Boolean = false,

        /**
         * True, if the user is allowed to create, rename, close, and reopen forum topics; for supergroups only
         */
        val canManageTopics: Boolean = false,

        /**
         * Custom title for this user
         */
        val customTitle: String? = null,
    ): TelegramChatMember("administrator", user)

    /**
     * Represents a chat member that has no additional privileges or restrictions.
     */
    data class Member(
        override val user: TelegramUser,

        /**
         * Date when the user's subscription will expire
         */
        val untilDate: ZonedDateTime? = null,
    ): TelegramChatMember("member", user)

    /**
     * Represents a chat member that is under certain restrictions in the chat. Supergroups only.
     */
    data class Restricted(
        override val user: TelegramUser,

        /**
         * True, if the user is a member of the chat at the moment of the request
         */
        val isMember: Boolean = false,

        /**
         * True, if the user is allowed to send text messages, contacts, giveaways, giveaway winners, invoices,
         * locations and venues
         */
        val canSendMessages: Boolean = false,

        /**
         * True, if the user is allowed to send audios
         */
        val canSendAudios: Boolean = false,

        /**
         * True, if the user is allowed to send documents
         */
        val canSendDocuments: Boolean = false,

        /**
         * True, if the user is allowed to send photos
         */
        val canSendPhotos: Boolean = false,

        /**
         * True, if the user is allowed to send videos
         */
        val canSendVideos: Boolean = false,

        /**
         * True, if the user is allowed to send video notes
         */
        val canSendVideoNotes: Boolean = false,

        /**
         * True, if the user is allowed to send voice notes
         */
        val canSendVoiceNotes: Boolean = false,

        /**
         * True, if the user is allowed to send polls
         */
        val canSendPolls: Boolean = false,

        /**
         * True, if the user is allowed to send animations, games, stickers and use inline bots
         */
        val canSendOtherMessages: Boolean = false,

        /**
         * True, if the user is allowed to add web page previews to their messages
         */
        val canAddWebPagePreviews: Boolean = false,

        /**
         * True, if the user is allowed to change the chat title, photo and other settings
         */
        val canChangeInfo: Boolean = false,

        /**
         * True, if the user is allowed to invite new users to the chat
         */
        val canInviteUsers: Boolean = false,

        /**
         * True, if the user is allowed to pin messages
         */
        val canPinMessages: Boolean = false,

        /**
         * True, if the user is allowed to create forum topics
         */
        val canManageTopics: Boolean = false,

        /**
         * Date when restrictions will be lifted for this user. If null, then the user is restricted forever
         */
        val untilDate: ZonedDateTime? = null,
    ): TelegramChatMember("restricted", user)

    /**
     * Represents a chat member that isn't currently a member of the chat, but may join it themselves.
     */
    data class Left(override val user: TelegramUser): TelegramChatMember("left", user)

    /**
     * Represents a chat member that was banned in the chat and can't return to the chat or view chat messages.
     */
    data class Banned(
        override val user: TelegramUser,

        /**
         * Date when restrictions will be lifted for this user. If null, then the user is banned forever
         */
        val untilDate: ZonedDateTime? = null,
    ): TelegramChatMember("kicked", user)
}