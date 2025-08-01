package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Represents a join request sent to a chat.
 */
data class TelegramChatJoinRequest(
    /**
     * Chat to which the request was sent
     */
    val chat: TelegramChat,

    /**
     * User that sent the join request
     */
    val from: TelegramUser,

    /**
     * Identifier of a private chat with the user who sent the join request.
     * The bot can use this identifier for 5 minutes to send messages until the join request is processed, assuming
     * no other administrator contacted the user.
     */
    val userChatId: Long,

    /**
     * Date the request was sent
     */
    val date: ZonedDateTime,

    /**
     * Bio of the user.
     */
    val bio: String? = null,

    /**
     * Chat invite link that was used by the user to send the join request
     */
    val inviteLink: TelegramChatInviteLink? = null,
)
