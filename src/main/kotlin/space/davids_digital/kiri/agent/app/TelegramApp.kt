package space.davids_digital.kiri.agent.app

import io.ktor.util.escapeHTML
import kotlinx.coroutines.runBlocking
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.integration.telegram.TelegramService

@AgentToolNamespace("telegram")
class TelegramApp(
    private val service: TelegramService,
): AgentApp("Telegram") {
    private var openedChatId: Long? = null

    override fun render(): List<DataFrame.ContentPart> {
        return if (openedChatId == null) {
            renderChatsList()
        } else {
            renderChat(openedChatId!!)
        }
    }

    private fun renderChatsList() = dataFrameContent {
        text(
            runBlocking {
                buildString {
                    appendLine("<chats>")
                    service.getChats().forEach { chat ->
                        val id = chat.id
                        val title = chat.title?.escapeHTML() ?: "<no_title/>"
                        appendLine("""<chat id="$id">$title</chat>""")
                    }
                    appendLine("</chats>")
                }
            }
        )
    }

    private fun renderChat(id: Long) = dataFrameContent {
        text(
            runBlocking {
                buildString {
                    val chat = service.getChat(id)
                    val title = chat.title?.escapeHTML()
                    val titleAttr = if (title != null) " title=\"$title\"" else ""
                    appendLine("""<chat id="$id"$titleAttr>""")
                    // TODO messages
                    appendLine("</chat>")
                }
            }
        )
    }

    @AgentToolMethod
    fun openChat(id: Long) {
        openedChatId = id
    }

    @AgentToolMethod(description = "Send message")
    fun send(message: String): String {
        val chatId = openedChatId ?: return "Chat is not opened"
        runBlocking {
            service.sendText(chatId, message)
        }
        return "Message sent"
    }

    override fun getAvailableAgentToolMethods(): List<Function<*>> {
        val result = mutableListOf<Function<*>>(::openChat)
        if (openedChatId != null) {
            result.add(::send)
        }
        return result
    }
}