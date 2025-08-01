package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Represents an invitation link for a chat.
 */
data class TelegramChatInviteLink (
    /**
     * The invite link. If the link was created by another chat administrator,
     * then the second part of the link will be replaced with “…”.
     */
    val inviteLink: String,

    /**
     * Creator of the link
     */
    val creator: TelegramUser,

    /**
     * True, if users joining the chat via the link need to be approved by chat administrators
     */
    val createsJoinRequest: Boolean = false,

    /**
     * True, if the link is primary
     */
    val isPrimary: Boolean = false,

    /**
     * True, if the link is revoked
     */
    val isRevoked: Boolean = false,

    /**
     * Invite link name
     */
    val name: String? = null,

    /**
     * Point in time when the link will expire or has been expired
     */
    val expireDate: ZonedDateTime? = null,

    /**
     * The maximum number of users that can be members of the chat simultaneously after joining the chat via this invite
     * link; 1-99999
     */
    val memberLimit: Int? = null,

    /**
     * Number of pending join requests created using this link
     */
    val pendingJoinRequestCount: Int? = null,

    /**
     * The number of seconds the subscription will be active for before the next payment
     */
    val subscriptionPeriod: Int? = null,

    /**
     * The amount of Telegram Stars a user must pay initially and after each subsequent subscription period to be a
     * member of the chat using the link
     */
    val subscriptionPrice: Int? = null,
)