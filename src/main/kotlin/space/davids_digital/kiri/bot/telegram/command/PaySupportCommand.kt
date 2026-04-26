package space.davids_digital.kiri.bot.telegram.command

import org.springframework.stereotype.Component
import space.davids_digital.kiri.integration.telegram.TelegramService

@Component
class PaySupportCommand(
    private val telegram: TelegramService,
    private val texts: PaymentTextSettings,
) : Command() {
    override val name = "paysupport"
    override val isPublic = true

    override suspend fun execute(context: CommandExecutionContext) {
        telegram.sendMessage(context.message.chatId, texts.paySupport())
    }
}
