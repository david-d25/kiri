package space.davids_digital.kiri.bot.telegram.command

import space.davids_digital.kiri.model.telegram.TelegramChat

object CommandUtils {
    val WHITESPACE_RE = "\\s+".toRegex()

    /**
     * Checks if the given message is a command.
     * The rules are:
     * * If the chat is a group (that is, [TelegramChat.Type.GROUP] or [TelegramChat.Type.SUPERGROUP]) and the message
     * has the format '@botusername /command [args...]', it's a command.
     * * If the chat is private (one-to-one) and the message begins with '/command', it's a command.
     * * In all other cases, it's not a command.
     *
     * Examples:
     * * `/myCommand arg1 arg2` (private chat)
     * * `@kiribot /help` (group chat)
     * * `@kiribot /myCommand arg1 arg2` (group chat)
     */
    fun isCommand(message: String, chatType: TelegramChat.Type, botUsername: String): Boolean {
        val text = message.trimStart()
        val normalizedBotUsername = botUsername.removePrefix("@")

        return when (chatType) {
            TelegramChat.Type.GROUP, TelegramChat.Type.SUPERGROUP -> {
                if (!text.startsWith("@")) return false
                val parts = text.split(WHITESPACE_RE, limit = 2)
                val mentionedUsername = parts[0].removePrefix("@")
                mentionedUsername.equals(normalizedBotUsername, ignoreCase = true)
                        && parts.size >= 2 && parts[1].startsWith("/")
            }
            else -> text.startsWith("/")
        }
    }
}