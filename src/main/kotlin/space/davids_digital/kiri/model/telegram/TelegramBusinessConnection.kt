package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Describes the connection of the bot with a business account.
 */
data class TelegramBusinessConnection (
    /**
     * 	Unique identifier of the business connection
     */
    val id: String,

    /**
     * 	Business account user that created the business connection
     */
    val user: TelegramUser,

    /**
     * 	Identifier of a private chat with the user who created the business connection.
     * 	This number may have more than 32 significant bits and some programming languages may have
     * 	difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a 64-bit integer or
     * 	double-precision float type are safe for storing this identifier.
     */
    val userChatId: Int,

    /**
     * Date the connection was established
     */
    val date: ZonedDateTime,

    /**
     * Rights of the business bot
     */
    val rights: TelegramBusinessBotRights? = null,

    /**
     * True, if the connection is active
     */
    val enabled: Boolean = true,
)