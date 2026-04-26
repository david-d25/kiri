package space.davids_digital.kiri.bot.telegram.command

import org.springframework.stereotype.Component
import space.davids_digital.kiri.integration.telegram.TelegramService

@Component
class SupportCommand(
    private val telegram: TelegramService,
    private val texts: PaymentTextSettings,
) : Command() {
    override val name = "support"
    override val isPublic = true

    override suspend fun execute(context: CommandExecutionContext) {
        telegram.sendMessage(context.message.chatId, texts.support())
    }
}
