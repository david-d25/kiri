package space.davids_digital.kiri.bot.telegram.command

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.integration.telegram.TelegramService

@Component
class ClearCommand(
    private val telegram: TelegramService,
    private val frameBuffer: FrameBuffer,
) : Command() {
    override val name = "clear"

    override suspend fun execute(context: CommandExecutionContext) {
        frameBuffer.clear()
        frameBuffer.addStatic {
            tag = "system"
            content = dataFrameContent {
                text("Framebuffer was cleared by external command")
            }
        }
        telegram.sendMessage(context.message.chatId, "Framebuffer cleared")
    }
}
