package space.davids_digital.kiri.agent.app

import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.integration.telegram.TelegramService

@AgentToolNamespace("telegram")
class TelegramApp(
    private val service: TelegramService,
): AgentApp("Telegram") {
    override fun render() = dataFrameContent {
        text("Not implemented yet")
    }

    @AgentToolMethod(description = "Send message")
    fun send(message: String): String {
        TODO("Not yet implemented")
    }

    override fun getAvailableAgentToolMethods() = listOf(::send)
}