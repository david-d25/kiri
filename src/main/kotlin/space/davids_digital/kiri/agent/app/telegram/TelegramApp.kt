package space.davids_digital.kiri.agent.app.telegram

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.DataFrameUtils
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramMessageOrmService
import space.davids_digital.kiri.service.TelegramNotificationService
import space.davids_digital.kiri.service.TemporaryFilesService
import kotlin.math.max
import kotlin.reflect.KFunction

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@AgentToolNamespace("telegram")
open class TelegramApp(
    private val telegram: TelegramService,
    private val renderer: TelegramAppRenderer,
    private val telegramNotificationService: TelegramNotificationService,
    private val chatOrm: TelegramChatOrmService,
    private val messageOrm: TelegramMessageOrmService,
    private val files: TemporaryFilesService
): AgentApp("telegram") {

    companion object {
        private const val CHATS_PAGE_SIZE = 10
        private const val MAX_MESSAGES_PER_VIEW = 8
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = LoggerFactory.getLogger(javaClass)

    private var selectedChatId: Long? = null

    override fun render(): List<DataFrame.ContentPart> = dataFrameContent {
        text("Telegram App opened.")
    }

    override fun getAvailableAgentToolMethods(): List<KFunction<*>> = buildList {
        add(::showUserProfile)
        if (selectedChatId == null) {
            add(::listChats)
            add(::selectChatById)
            add(::selectChatByUsername)
        } else {
            add(::listLatestMessages)
            add(::send)
            add(::sendSticker)
            add(::closeChat)
            add(::download)
        }
    }

    @AgentToolMethod
    suspend fun listChats(
        @AgentToolParameter(description = "zero-based page index")
        page: Int = 0
    ): List<DataFrame.ContentPart> {
        val chats = chatOrm.findAllEnabled(PageRequest.of(page, CHATS_PAGE_SIZE))

        return dataFrameContent {
            with (renderer) {
                renderChatsPage(chats)
            }
        }
    }

    @AgentToolMethod
    suspend fun selectChatById(id: Long): String {
        val newChat = telegram.getChat(id) ?: return "Chat with id $id not found."
        val previousChatId = selectedChatId
        selectedChatId = newChat.id
        if (previousChatId != null) {
            telegramNotificationService.onChatClosedInAgentApp(previousChatId)
        }
        telegramNotificationService.onChatOpenedInAgentApp(newChat.id)
        return "Chat selected."
    }

    @AgentToolMethod
    suspend fun selectChatByUsername(username: String): String {
        val newChat = telegram.getChat(username.removePrefix("@")) ?: return "Chat with username '$username' not found."
        val previousChatId = selectedChatId
        selectedChatId = newChat.id
        if (previousChatId != null) {
            telegramNotificationService.onChatClosedInAgentApp(previousChatId)
        }
        telegramNotificationService.onChatOpenedInAgentApp(newChat.id)
        return "Chat selected."
    }

    @AgentToolMethod
    suspend fun closeChat(): String {
        val chatId = selectedChatId ?: return "No chat is currently selected."
        selectedChatId = null
        telegramNotificationService.onChatClosedInAgentApp(chatId)
        return "Chat closed."
    }

    @AgentToolMethod
    suspend fun listLatestMessages(): List<DataFrame.ContentPart> {
        val chatId = selectedChatId ?: error("No chat is currently selected.")
        val chat = telegram.getChat(chatId) ?: error("Chat with id $chatId not found.")
        val messages = messageOrm.findOrderedByMessageIdDesc(chatId, MAX_MESSAGES_PER_VIEW)
        val lastMessage = messages.maxByOrNull { it.messageId }
        val laterMessagesRemaining = lastMessage?.let { messageOrm.countMessagesAfterId(chatId, it.messageId) } ?: 0
        val lastReadMessageId = chat.metadata.lastReadMessageId
        val laterNewMessagesRemaining = lastReadMessageId?.let { messageOrm.countMessagesAfterId(chatId, it) } ?: 0L
        lastMessage?.let { setLastReadMessageId(chatId, it.messageId) }
        return dataFrameContent {
            with (renderer) {
                renderChat(chat, messages, laterMessagesRemaining, laterNewMessagesRemaining)
            }
        }
    }

//    @AgentToolMethod(description = "search messages based on the specified filters")
//    suspend fun searchMessages(
//        textContains: String? = null,
//        @AgentToolParameter("format: ${DataFrameUtils.PRETTY_DATE_TIME_PATTERN}")
//        beforeDate: String? = null,
//        @AgentToolParameter("format: ${DataFrameUtils.PRETTY_DATE_TIME_PATTERN}")
//        afterDate: String? = null,
//        userId: Long? = null,
//    ): List<DataFrame.ContentPart> {
//        val chatId = selectedChatId ?: error("No chat is currently selected.")
//        val chat = telegram.getChat(chatId) ?: error("Chat with id $chatId not found.")
//
//        // TODO
//    }

    @AgentToolMethod
    suspend fun showUserProfile(username: String): List<DataFrame.ContentPart> {
        val cleanUsername = username.removePrefix("@")
        val user = telegram.getUserByUsername(cleanUsername) ?: return dataFrameContent {
            text("User with username @$cleanUsername not found.")
        }
        val chat = telegram.getChat(cleanUsername)
        return dataFrameContent {
            with (renderer) {
                renderUserProfile(user, chat)
            }
        }
    }

    @AgentToolMethod
    suspend fun showStickerPack(id: String) {
        // TODO
    }

    @AgentToolMethod
    suspend fun download(fileId: String): String {
        val bytes = telegram.getFileContent(fileId)
        val fileName = "file_${fileId}"
        val createdFile = files.create(fileName, bytes)
        return "Saved as temporary file: $fileName"
    }

    @AgentToolMethod
    suspend fun sendSticker(fileId: String): String {
        val selectedChatId = selectedChatId ?: return "Chat not opened"
        telegram.sendSticker(selectedChatId, fileId)
        return "sent"
    }

    @AgentToolMethod(
        description = "Send message. " +
                "Supported HTML tags: b, i, u, s, tg-spoiler, a[href], tg-emoji[emoji-id], code, pre, blockquote, " +
                "blockquote[expandable]."
    )
    open suspend fun send(
        message: String,

        @AgentToolParameter(description = "id of message to reply to")
        replyTo: Int? = null,

        @AgentToolParameter(description = "images as filenames")
        images: List<String> = emptyList()
    ): String {
        val imageContents = images.map { files.getContent(it) ?: error("file '$it' not found") }
        val selectedChatId = selectedChatId ?: return "Chat not opened"
        telegram.sendMessage(selectedChatId, message, replyToMessageId = replyTo, images = imageContents)
        return "sent"
    }

    override suspend fun onClose() {
        val selectedChatId = selectedChatId
        if (selectedChatId != null) {
            telegramNotificationService.onChatClosedInAgentApp(selectedChatId)
        }
        scope.cancel("App is closing")
    }

    private fun setLastReadMessageId(chatId: Long, messageId: Int) {
        val openedChat = chatOrm.findById(chatId) ?: error("chat with id $chatId found")
        if (openedChat.metadata.lastReadMessageId != null && messageId <= openedChat.metadata.lastReadMessageId) {
            return // Don't move up read cursor
        }
        chatOrm.save(openedChat.copy(
            metadata = openedChat.metadata.copy(
                lastReadMessageId = messageId
            )
        ))
    }
}