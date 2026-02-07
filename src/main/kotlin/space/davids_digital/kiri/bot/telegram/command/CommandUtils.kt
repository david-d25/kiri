package space.davids_digital.kiri.bot.telegram.command

import space.davids_digital.kiri.model.telegram.TelegramChat

object CommandUtils {
    /**
     * Checks if the given message is a command.
     * The rules are:
     * * If the chat is a group (that is, [TelegramChat.Type.GROUP] or [TelegramChat.Type.SUPERGROUP]) and the message
     * beginning has the format '/command@username' where username is the bot's username, it's a command.
     * * If the chat is private (one-to-one) and the message begins with either '/command' or
     * '/command@username' where username is the bot's username, it's a command.
     * * In all other cases, it's not a command.
     *
     * Examples:
     * * `/myCommand arg1 arg2`
     * * `/help@kiribot`
     */
    fun isCommand(message: String, chatType: TelegramChat.Type, botUsername: String): Boolean {
        val text = message.trimStart()
        if (!text.startsWith("/")) {
            return false
        }

        val token = text.substring(1).split("\\s+".toRegex(), limit = 2)[0]
        val normalizedBotUsername = botUsername.removePrefix("@")
        val atIndex = token.indexOf('@')

        return when (chatType) {
            TelegramChat.Type.GROUP, TelegramChat.Type.SUPERGROUP ->
                atIndex > 0 && token.substring(atIndex + 1).equals(normalizedBotUsername, ignoreCase = true)
            else -> true
        }
    }
}