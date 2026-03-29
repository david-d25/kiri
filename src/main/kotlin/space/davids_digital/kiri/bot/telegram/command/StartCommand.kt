package space.davids_digital.kiri.bot.telegram.command

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.engine.AgentEngine
import space.davids_digital.kiri.integration.telegram.TelegramService

@Component
class StartCommand(
    private val telegram: TelegramService,
    private val engine: AgentEngine,
) : Command() {
    override val name = "start"

    override suspend fun execute(context: CommandExecutionContext) {
        try {
            engine.start()
            telegram.sendMessage(context.message.chatId, "Start requested")
        } catch (e: Exception) {
            log.error("Error starting engine", e)
            telegram.sendMessage(context.message.chatId, "Error starting engine")
        }
    }
}