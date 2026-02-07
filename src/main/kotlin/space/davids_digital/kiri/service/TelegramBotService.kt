package space.davids_digital.kiri.service

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.bot.telegram.command.CommandExecutionContext
import space.davids_digital.kiri.bot.telegram.command.CommandUtils.isCommand
import space.davids_digital.kiri.bot.telegram.command.CommandsHolder
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.User
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.service.UserOrmService
import javax.annotation.PostConstruct

@Service
class TelegramBotService (
    private val telegram: TelegramService,
    private val users: UserOrmService,
    private val commandsHolder: CommandsHolder,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @PostConstruct
    private fun init() {
        startUpdateProcessing()
    }

    @PreDestroy
    private fun destroy() {
        scope.cancel()
    }

    private fun startUpdateProcessing() {
        scope.launch {
            telegram.updates.collect { update ->
                try {
                    if (update.message != null) {
                        onMessage(update.message)
                    }
                } catch (e: Exception) {
                    log.error("Error handling update", e)
                }
            }
        }
    }

    private suspend fun onMessage(message: TelegramMessage) {
        val chat = telegram.fetchAndSaveChatById(message.chatId)
        if (chat == null) {
            log.warn("Received message from unknown chat with id {}", message.chatId)
            return
        }
        val textOrCaption = message.text ?: message.caption ?: return
        if (!isCommand(textOrCaption, chat.type, telegram.getSelf().username ?: "")) {
            return
        }
        val fromId = message.fromId ?: return
        val userRole = withContext(Dispatchers.IO) {
            users.findById(fromId)
        }?.role
        // Should probably refactor for something more clever
        if (userRole != User.Role.ADMIN && userRole != User.Role.OWNER) {
            telegram.sendMessage(message.chatId, "👮‍♂️ No permissions")
            return
        }
        if (message.forwardOrigin != null) {
            return // Ignore forwarded messages
        }
        val context = parseCommand(message)
        if (context == null) {
            telegram.sendMessage(message.chatId, "Bad command format")
            return
        }
        val command = commandsHolder.commands.find { it.name == context.commandName }
        if (command == null) {
            telegram.sendMessage(message.chatId, "Command not found")
            return
        }
        scope.launch {
            try {
                command.execute(context)
            } catch (e: Exception) {
                telegram.sendMessage(message.chatId, "Error executing command: ${e.message}")
            }
        }
    }

    private fun parseCommand(message: TelegramMessage): CommandExecutionContext? {
        val textOrCaption = message.text ?: message.caption ?: return null

        // Parsing command name and omitting bot username, if present
        val token = textOrCaption.trimStart().substring(1).split("\\s+".toRegex(), limit = 2)[0]
        val commandName = token.substringBefore('@')

        // Parsing arguments
        val arguments = textOrCaption.trimStart().substring(1).split("\\s+".toRegex()).drop(1)

        return CommandExecutionContext(
            commandName = commandName,
            arguments = arguments,
            message = message
        )
    }
}