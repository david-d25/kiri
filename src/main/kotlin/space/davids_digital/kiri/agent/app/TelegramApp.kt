package space.davids_digital.kiri.agent.app

import io.ktor.util.escapeHTML
import kotlinx.coroutines.runBlocking
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.asPrettyString
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.telegram.TelegramChat

@AgentToolNamespace("telegram")
class TelegramApp(
    private val service: TelegramService
): AgentApp("telegram") {
    private var openedChatId: Long? = null

    override fun render(): List<DataFrame.ContentPart> {
        return if (openedChatId == null) {
            renderChatList()
        } else {
            renderChat(openedChatId!!)
        }
    }

    private fun renderChatList() = dataFrameContent {
        text(
            runBlocking {
                buildString {
                    val chats = service.getChats()
                    appendLine("<chats>")
                    if (chats.isNotEmpty()) {
                        chats.forEach { renderChatListItem(it) }
                    } else {
                        appendLine("<no_chats/>")
                    }
                    appendLine("</chats>")
                }
            }
        )
    }

    private fun StringBuilder.renderChatListItem(chat: TelegramChat) {
        when (chat.type) {
            TelegramChat.Type.PRIVATE -> {
                val id = chat.id
                val user = service.getUser(id)
                val displayName = user?.let{ user.firstName + (user.lastName?.let { " $it" } ?: "") }
                    ?: "(unknown user)"
                appendLine("""<private-chat id="$id">$displayName</private-chat>""")
            }
            TelegramChat.Type.GROUP, TelegramChat.Type.SUPERGROUP -> {
                val id = chat.id
                val title = chat.title?.escapeHTML() ?: "<no_title/>"
                appendLine("""<group-chat id="$id">$title</private-chat>""")
            }
            else -> {
                val id = chat.id
                val type = chat.type.name
                appendLine("""<chat id="$id" type="$type"/>""")
            }
        }
    }

    private fun renderChat(id: Long) = dataFrameContent {
        text(
            runBlocking {
                buildString {
                    val chat = service.getChat(id)
                    val title = chat.title?.escapeHTML()
                    val titleAttr = if (title != null) " title=\"$title\"" else ""
                    appendLine("""<chat id="$id"$titleAttr>""")
                    val chatMessages = service.getChatMessages(id, 20)
                    chatMessages.forEach {
                        val text = it.text?.escapeHTML()
                        val sentAt = it.date.asPrettyString()
                        val from = it.fromId
                            ?.let { service.getUser(it) }
                            ?.let { it.firstName + (it.lastName?.let { " $it" } ?: "") }
                        appendLine("""<message from="$from" sent-at="$sentAt">$text</message>""")
                    }
                    if (chatMessages.isEmpty()) {
                        appendLine("<no_messages/>")
                    }
                    appendLine("</chat>")
                }
            }
        )
    }

    @AgentToolMethod
    suspend fun openChat(id: Long): String {
        if (service.chatExists(id)) {
            openedChatId = id
        } else {
            return "Chat with id $id does not exist"
        }
        return "ok"
    }

    @AgentToolMethod(description = "Goes back to chat list")
    fun closeChat() {
        openedChatId = null
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
        } else {
            result.add(::closeChat)
        }
        return result
    }
}