package space.davids_digital.kiri.bot.telegram.command

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.engine.AgentEngine
import space.davids_digital.kiri.integration.telegram.TelegramService

@Component
class StopCommand(
    private val telegram: TelegramService,
    private val engine: AgentEngine,
) : Command() {
    override val name = "stop"

    override suspend fun execute(context: CommandExecutionContext) {
        try {
            engine.softStop()
            telegram.sendMessage(context.message.chatId, "Stop requested")
        } catch (e: Exception) {
            log.error("Error stopping engine", e)
            telegram.sendMessage(context.message.chatId, "Error stopping engine")
        }
    }
}