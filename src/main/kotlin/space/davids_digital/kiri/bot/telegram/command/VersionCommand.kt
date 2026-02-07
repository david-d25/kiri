package space.davids_digital.kiri.bot.telegram.command

import org.springframework.stereotype.Component
import space.davids_digital.kiri.AppProperties
import space.davids_digital.kiri.integration.telegram.TelegramService

@Component
class VersionCommand(
    private val properties: AppProperties,
    private val telegram: TelegramService
) : Command() {
    override val name = "version"

    override suspend fun execute(context: CommandExecutionContext) {
        telegram.sendMessage(context.message.chatId, properties.version)
    }
}