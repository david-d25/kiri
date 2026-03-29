package space.davids_digital.kiri.agent.app.telegram

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.DataFrameUtils.PRETTY_DATE_TIME_PATTERN
import space.davids_digital.kiri.agent.frame.DataFrameUtils.fromPrettyStringToZonedDateTime
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageEntity
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramMessageOrmService
import space.davids_digital.kiri.orm.specifications.telegram.TelegramMessageSpecifications
import space.davids_digital.kiri.service.TelegramNotificationService
import space.davids_digital.kiri.service.TemporaryFilesService
import kotlin.reflect.KFunction

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@AgentToolNamespace("telegram")
class TelegramApp(
    private val telegram: TelegramService,
    private val renderer: TelegramAppRenderer,
    private val telegramNotificationService: TelegramNotificationService,
    private val chatOrm: TelegramChatOrmService,
    private val messageOrm: TelegramMessageOrmService,
    private val files: TemporaryFilesService
): AgentApp("telegram") {

    companion object {
        private const val CHATS_PAGE_SIZE = 10
        private const val MAX_MESSAGES_PER_VIEW = 10
    }

    private val log = LoggerFactory.getLogger(javaClass)

    private var selectedChatId: Long? = null

    override fun render(): List<DataFrame.ContentPart> = dataFrameContent {
        text("Telegram App opened.")
        val chatId = selectedChatId
        if (chatId != null) {
            val chat = runBlocking(Dispatchers.IO) { chatOrm.findById(chatId) }
            val title = chat?.title ?: chat?.firstName ?: chatId.toString()
            val unreadCount = runBlocking(Dispatchers.IO) {
                messageOrm.countMessagesAfterId(chatId, chat?.metadata?.lastReadMessageId ?: 0)
            }
            line("")
            text("Selected chat: $title (id $chatId). Unread messages: $unreadCount")
        }
    }

    override fun getAvailableAgentToolMethods(): List<KFunction<*>> = buildList {
        add(::getUserProfile)
        add(::listChats)
        add(::switchToChatById)
        add(::switchToChatByUsername)
        if (selectedChatId != null) {
            add(::listLatestMessages)
            add(::searchMessages)
            add(::send)
            add(::sendSticker)
            add(::closeChat)
            add(::getChatInfo)
            add(::download)
        }
    }

    @AgentToolMethod
    suspend fun listChats(
        @AgentToolParameter(description = "zero-based page index")
        page: Int = 0
    ): List<DataFrame.ContentPart> {
        val chats = withContext(Dispatchers.IO) {
            chatOrm.findAllEnabled(PageRequest.of(page, CHATS_PAGE_SIZE))
        }
        val unreadCounts = withContext(Dispatchers.IO) {
            chats.associate { chat ->
                chat.id to messageOrm.countMessagesAfterId(chat.id, chat.metadata.lastReadMessageId ?: 0)
            }
        }

        return dataFrameContent {
            with (renderer) {
                renderChatsPage(chats, unreadCounts)
            }
        }
    }

    @AgentToolMethod
    suspend fun switchToChatById(id: Long): String {
        val newChat = telegram.fetchAndSaveChatById(id) ?: return "Chat with id $id not found."
        return selectChat(newChat)
    }

    @AgentToolMethod
    suspend fun switchToChatByUsername(username: String): String {
        val cleanUsername = username.removePrefix("@")
        val newChat = withContext(Dispatchers.IO) { chatOrm.findByUsername(cleanUsername) }
            ?: telegram.fetchAndSaveChatByUsername(cleanUsername)
            ?: return "Chat with username '$username' not found."
        return selectChat(newChat)
    }

    private suspend fun selectChat(chat: TelegramChat): String {
        val previousChatId = selectedChatId
        selectedChatId = chat.id
        if (previousChatId != null) {
            telegramNotificationService.onChatClosedInAgentApp(previousChatId)
        }
        telegramNotificationService.onChatOpenedInAgentApp(chat.id)
        val title = chat.title ?: chat.firstName
        val unreadCount = withContext(Dispatchers.IO) {
            messageOrm.countMessagesAfterId(chat.id, chat.metadata.lastReadMessageId ?: 0)
        }
        return if (unreadCount > 0) {
            "Chat selected. Title: $title. Unread messages: $unreadCount"
        } else {
            "Chat selected. Title: $title. No unread messages."
        }
    }

    @AgentToolMethod
    suspend fun closeChat(): String {
        val chatId = selectedChatId ?: return "No chat is currently selected."
        selectedChatId = null
        telegramNotificationService.onChatClosedInAgentApp(chatId)
        return "Chat closed."
    }

    @AgentToolMethod(description = "Get brief or full information about the currently selected chat")
    suspend fun getChatInfo(
        @AgentToolParameter(description = "show all info, i.e. avatar and description")
        full: Boolean = false
    ): List<DataFrame.ContentPart> {
        val chatId = selectedChatId ?: return dataFrameContent {
            text("No chat is currently selected.")
        }
        val chat = telegram.fetchAndSaveChatById(chatId) ?: return dataFrameContent {
            text("Chat with id $chatId not found.")
        }
        return dataFrameContent {
            with (renderer) {
                renderChatInfo(chat, full)
            }
        }
    }

    @AgentToolMethod(description = "show chat latest messages; can show up to $MAX_MESSAGES_PER_VIEW")
    suspend fun listLatestMessages(
        @AgentToolParameter(description = "Number of messages to list; 0 = show only unread, but no more than $MAX_MESSAGES_PER_VIEW")
        n: Int = 0,
        @AgentToolParameter(description = "if true, show full XML with nested replies, all metadata, thumbnails. Default: compact plain-text.")
        detailed: Boolean = false
    ): List<DataFrame.ContentPart> {
        require(n >= 0) { "Number of messages must not be negative, got $n" }
        val nSafe = if (n > 0) n.coerceAtMost(MAX_MESSAGES_PER_VIEW) else MAX_MESSAGES_PER_VIEW
        val chatId = selectedChatId ?: error("No chat is currently selected.")
        val chat = telegram.fetchAndSaveChatById(chatId) ?: error("Chat with id $chatId not found.")
        val lastReadMessageId = chat.metadata.lastReadMessageId
        val messages = withContext(Dispatchers.IO) {
            if (n == 0 && lastReadMessageId != null) {
                messageOrm.findAfterMessageIdOrderedByMessageIdDesc(chatId, lastReadMessageId, nSafe)
            } else {
                messageOrm.findOrderedByMessageIdDesc(chatId, nSafe)
            }
        }
        val lastMessage = messages.maxByOrNull { it.messageId }
        val laterMessagesRemaining = withContext(Dispatchers.IO) {
            lastMessage?.let { messageOrm.countMessagesAfterId(chatId, it.messageId) } ?: 0
        }
        val laterNewMessagesRemaining = withContext(Dispatchers.IO) {
            messageOrm.countMessagesAfterId(chatId, lastReadMessageId ?: 0)
        }
        withContext(Dispatchers.IO) {
            lastMessage?.let { setLastReadMessageId(chatId, it.messageId) }
        }
        return dataFrameContent {
            with (renderer) {
                if (detailed) {
                    renderMessages(
                        chat.title, chatId,
                        chat.metadata.lastReadMessageId,
                        messages,
                        laterMessagesRemaining,
                        laterNewMessagesRemaining
                    )
                } else {
                    renderMessagesCompact(
                        chat.title, chatId,
                        chat.metadata.lastReadMessageId,
                        messages,
                        laterMessagesRemaining,
                        laterNewMessagesRemaining
                    )
                }
            }
        }
    }

    @AgentToolMethod(description = "search/filter messages in the current chat; returns newest-first by default")
    suspend fun searchMessages(
        @AgentToolParameter(description = "filter by text (case-insensitive substring match)")
        textContains: String? = null,
        @AgentToolParameter(description = "filter by sender user id")
        fromUserId: Long? = null,
        @AgentToolParameter(description = "only messages before this date; format: $PRETTY_DATE_TIME_PATTERN")
        beforeDate: String? = null,
        @AgentToolParameter(description = "only messages after this date; format: $PRETTY_DATE_TIME_PATTERN")
        afterDate: String? = null,
        @AgentToolParameter(description = "only messages before this message id (exclusive)")
        beforeId: Int? = null,
        @AgentToolParameter(description = "only messages after this message id (exclusive)")
        afterId: Int? = null,
        @AgentToolParameter(description = "max number of messages to return (max $MAX_MESSAGES_PER_VIEW)")
        limit: Int = MAX_MESSAGES_PER_VIEW,
        @AgentToolParameter(description = "'newest_first' (default) or 'oldest_first'")
        order: String = "newest_first",
        @AgentToolParameter(description = "if true, show full XML with nested replies, all metadata, thumbnails. Default: compact plain-text.")
        detailed: Boolean = false
    ): List<DataFrame.ContentPart> {
        val chatId = selectedChatId ?: error("No chat is currently selected.")
        val chat = telegram.fetchAndSaveChatById(chatId) ?: error("Chat with id $chatId not found.")
        val limitSafe = limit.coerceIn(1, MAX_MESSAGES_PER_VIEW)

        var spec: Specification<TelegramMessageEntity> = TelegramMessageSpecifications.chatId(chatId)
        textContains?.let { spec = spec.and(TelegramMessageSpecifications.textContains(it)) }
        fromUserId?.let { spec = spec.and(TelegramMessageSpecifications.fromUser(it)) }
        beforeDate?.let {
            spec = spec.and(TelegramMessageSpecifications.dateBefore(it.fromPrettyStringToZonedDateTime().toOffsetDateTime()))
        }
        afterDate?.let {
            spec = spec.and(TelegramMessageSpecifications.dateAfter(it.fromPrettyStringToZonedDateTime().toOffsetDateTime()))
        }
        beforeId?.let { spec = spec.and(TelegramMessageSpecifications.messageIdLessThan(it)) }
        afterId?.let { spec = spec.and(TelegramMessageSpecifications.messageIdGreaterThan(it)) }

        val sortDirection = if (order == "oldest_first") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(0, limitSafe, Sort.by(sortDirection, "id.messageId"))
        val messages = withContext(Dispatchers.IO) {
            messageOrm.search(spec, pageable)
        }

        return dataFrameContent {
            with(renderer) {
                if (detailed) {
                    renderMessages(
                        chat.title, chatId,
                        chat.metadata.lastReadMessageId,
                        messages,
                        laterMessagesRemaining = 0,
                        laterNewMessagesRemaining = 0
                    )
                } else {
                    renderMessagesCompact(
                        chat.title, chatId,
                        chat.metadata.lastReadMessageId,
                        messages,
                        laterMessagesRemaining = 0,
                        laterNewMessagesRemaining = 0
                    )
                }
            }
        }
    }

    @AgentToolMethod
    suspend fun getUserProfile(username: String): List<DataFrame.ContentPart> {
        val cleanUsername = username.removePrefix("@")
        val user = telegram.getUserByUsername(cleanUsername) ?: return dataFrameContent {
            text("User with username @$cleanUsername not found.")
        }
        val chat = telegram.fetchAndSaveChatById(user.id)
        return dataFrameContent {
            with (renderer) {
                renderUserProfile(user, chat)
            }
        }
    }

    @AgentToolMethod
    suspend fun download(fileId: String): String {
        val bytes = telegram.getFileContent(fileId)
        val fileName = "file_${fileId}"
        files.create(fileName, bytes)
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
    suspend fun send(
        message: String,

        @AgentToolParameter(description = "id of message to reply to")
        replyTo: Int? = null,

        @AgentToolParameter(description = "images as filenames")
        images: List<String> = emptyList()
    ): String {
        val selectedChatId = selectedChatId ?: return "Chat not opened"
        val imageContents = images.map { files.getContent(it) ?: error("file '$it' not found") }
        telegram.sendMessage(selectedChatId, message, replyToMessageId = replyTo, images = imageContents)
        return "sent"
    }

    override suspend fun onClose() {
        val selectedChatId = selectedChatId
        if (selectedChatId != null) {
            telegramNotificationService.onChatClosedInAgentApp(selectedChatId)
        }
    }

    private fun setLastReadMessageId(chatId: Long, messageId: Int) {
        val openedChat = chatOrm.findById(chatId) ?: error("chat with id $chatId not found")
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