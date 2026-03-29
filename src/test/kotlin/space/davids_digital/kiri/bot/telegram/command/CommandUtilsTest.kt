package space.davids_digital.kiri.bot.telegram.command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import space.davids_digital.kiri.model.telegram.TelegramChat

class CommandUtilsTest {

    private val botUsername = "kiribot"

    @Nested
    inner class PrivateChat {
        private val chatType = TelegramChat.Type.PRIVATE

        @Test
        fun `slash command is recognized`() {
            assertTrue(CommandUtils.isCommand("/help", chatType, botUsername))
        }

        @Test
        fun `slash command with arguments is recognized`() {
            assertTrue(CommandUtils.isCommand("/set arg1 arg2", chatType, botUsername))
        }

        @Test
        fun `plain text is not a command`() {
            assertFalse(CommandUtils.isCommand("hello", chatType, botUsername))
        }

        @Test
        fun `mention format is not a command in private chat`() {
            assertFalse(CommandUtils.isCommand("@kiribot /help", chatType, botUsername))
        }

        @Test
        fun `leading whitespace is trimmed`() {
            assertTrue(CommandUtils.isCommand("  /help", chatType, botUsername))
        }
    }

    @Nested
    inner class GroupChat {
        private val chatType = TelegramChat.Type.GROUP

        @Test
        fun `mention then command is recognized`() {
            assertTrue(CommandUtils.isCommand("@kiribot /help", chatType, botUsername))
        }

        @Test
        fun `mention then command with arguments is recognized`() {
            assertTrue(CommandUtils.isCommand("@kiribot /set arg1 arg2", chatType, botUsername))
        }

        @Test
        fun `case insensitive username match`() {
            assertTrue(CommandUtils.isCommand("@KiriBot /help", chatType, botUsername))
        }

        @Test
        fun `wrong bot username is not a command`() {
            assertFalse(CommandUtils.isCommand("@otherbot /help", chatType, botUsername))
        }

        @Test
        fun `mention without command is not a command`() {
            assertFalse(CommandUtils.isCommand("@kiribot hello", chatType, botUsername))
        }

        @Test
        fun `mention only is not a command`() {
            assertFalse(CommandUtils.isCommand("@kiribot", chatType, botUsername))
        }

        @Test
        fun `bare slash command without mention is not recognized in group`() {
            assertFalse(CommandUtils.isCommand("/help", chatType, botUsername))
        }

        @Test
        fun `leading whitespace is trimmed`() {
            assertTrue(CommandUtils.isCommand("  @kiribot /help", chatType, botUsername))
        }

        @Test
        fun `bot username with at-prefix in parameter works`() {
            assertTrue(CommandUtils.isCommand("@kiribot /help", chatType, "@kiribot"))
        }
    }

    @Nested
    inner class SupergroupChat {
        private val chatType = TelegramChat.Type.SUPERGROUP

        @Test
        fun `mention then command is recognized`() {
            assertTrue(CommandUtils.isCommand("@kiribot /start", chatType, botUsername))
        }

        @Test
        fun `bare slash command is not recognized`() {
            assertFalse(CommandUtils.isCommand("/start", chatType, botUsername))
        }
    }
}
