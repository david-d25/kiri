package space.davids_digital.kiri.bot.telegram.command

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.engine.AgentEngine
import space.davids_digital.kiri.integration.telegram.TelegramService

@Component
class HardStopCommand(
    private val telegram: TelegramService,
    private val engine: AgentEngine,
) : Command() {
    override val name = "hardstop"

    override suspend fun execute(context: CommandExecutionContext) {
        try {
            engine.hardStop()
            telegram.sendMessage(context.message.chatId, "Engine off")
        } catch (e: Exception) {
            log.error("Error hard-stopping engine", e)
            telegram.sendMessage(context.message.chatId, "Error stopping engine")
        }
    }
}