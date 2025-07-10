package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * This object represents changes in the status of a chat member.
 */
data class TelegramChatMemberUpdated (
    /**
     * Chat the user belongs to
     */
    val chat: TelegramChat,

    /**
     * Performer of the action, which resulted in the change
     */
    val from: TelegramUser,

    /**
     * Date the change was done
     */
    val date: ZonedDateTime,

    /**
     * Previous information about the chat member
     */
    val oldChatMember: TelegramChatMember,

    /**
     * New information about the chat member
     */
    val newChatMember: TelegramChatMember,

    /**
     * Chat invite link, which was used by the user to join the chat; for joining by invite link events only.
     */
    val inviteLink: TelegramChatInviteLink? = null,

    /**
     * True, if the user joined the chat after sending a direct join request without using an invitation link and
     * being approved by an administrator
     */
    val viaJoinRequest: Boolean = false,

    /**
     * True, if the user joined the chat via a chat folder invite link
     */
    val viaChatFolderInviteLink: Boolean = false
)