package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

/**
 * Represents the origin of a Telegram message.
 */
sealed class TelegramMessageOrigin() {
    /**
     * The message was originally sent by a known user.
     */
    data class User(
        /**
         * Date the message was sent.
         */
        val date: ZonedDateTime,
        /**
         * User that sent the message originally.
         */
        val senderUserId: Long,
    ) : TelegramMessageOrigin()

    /**
     * The message was originally sent by an unknown user.
     */
    data class HiddenUser(
        /**
         * Date the message was sent.
         */
        val date: ZonedDateTime,
        /**
         * Name of the user that sent the message originally.
         */
        val senderUserName: String,
    ) : TelegramMessageOrigin()

    /**
     * The message was originally sent on behalf of a chat to a group chat.
     */
    data class Chat(
        /**
         * Date the message was sent.
         */
        val date: ZonedDateTime,
        /**
         * Chat that sent the message originally.
         */
        val senderChatId: Long,
        /**
         * For messages originally sent by an anonymous chat administrator, original message author signature.
         */
        val authorSignature: String? = null,
    ) : TelegramMessageOrigin()

    /**
     * The message was originally sent to a channel chat.
     */
    data class Channel(
        /**
         * Date the message was sent.
         */
        val date: ZonedDateTime,
        /**
         * Channel chat to which the message was originally sent.
         */
        val chatId: Long,
        /**
         * Unique message identifier inside the chat.
         */
        val messageId: Int,
        /**
         * Signature of the original post author.
         */
        val authorSignature: String? = null
    ) : TelegramMessageOrigin()

    data class Unknown(
        /**
         * Date the message was sent.
         */
        val date: ZonedDateTime,
    ) : TelegramMessageOrigin()
}