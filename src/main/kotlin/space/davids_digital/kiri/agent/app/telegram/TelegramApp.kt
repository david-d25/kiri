package space.davids_digital.kiri.agent.app.telegram

import jakarta.transaction.Transactional
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramMessageOrmService
import space.davids_digital.kiri.service.TelegramNotificationService
import kotlin.math.max

@AgentToolNamespace("telegram")
open class TelegramApp(
    private val telegram: TelegramService,
    private val renderer: TelegramAppRenderer,
    private val telegramNotificationService: TelegramNotificationService,
    private val chatOrm: TelegramChatOrmService,
    private val messageOrm: TelegramMessageOrmService
): AgentApp("telegram") {

    companion object {
        private const val CHATS_PAGE_SIZE = 10
        private const val MAX_MESSAGES_PER_VIEW = 12
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = LoggerFactory.getLogger(javaClass)

    private var viewState = TelegramAppViewState()
    private var autoscrollToEnd = false

    @Transactional
    override fun render(): List<DataFrame.ContentPart> {
        if (viewState.openedChat != null && autoscrollToEnd) {
            scrollToBottom()
        } else {
            updateLaterMessagesRemaining()
        }
        return renderer.render(viewState)
    }

    @AgentToolMethod
    suspend fun openChat(id: Long): String {
        val newChat = telegram.getChat(id)
        if (newChat != null) {
            val previousChat = viewState.openedChat
            if (previousChat != null) {
                telegramNotificationService.onChatClosedInAgentApp(previousChat.id)
            }
            showChat(newChat)
            telegramNotificationService.onChatOpenedInAgentApp(id)
        } else {
            return "Chat with id $id does not exist"
        }
        return "ok"
    }

    @AgentToolMethod
    suspend fun openChatByUsername(username: String): String {
        val newChat = telegram.getChat(username.removePrefix("@"))
        if (newChat != null) {
            val previousChat = viewState.openedChat
            if (previousChat != null) {
                telegramNotificationService.onChatClosedInAgentApp(previousChat.id)
            }
            showChat(newChat)
            telegramNotificationService.onChatOpenedInAgentApp(newChat.id)
        } else {
            return "Chat with username $username not found"
        }
        return "ok"
    }

    @AgentToolMethod(description = "Open list of chats. Will close current chat.")
    fun chatList(pageNumber: Int? = null) {
        val openedChat = viewState.openedChat
        if (openedChat != null) {
            telegramNotificationService.onChatClosedInAgentApp(openedChat.id)
        }
        showChats(pageNumber ?: (viewState.chatsPageIndex + 1))
    }

    @AgentToolMethod(description = "Scroll messages; positive = down, negative = up")
    fun scroll(amount: Int): String {
        if (amount > 0 && autoscrollToEnd) {
            return "already at bottom"
        }
        val chatId = viewState.openedChat?.id ?: return "Chat not opened"
        val viewLastMessage = viewState.messagesView.lastOrNull() ?: return "No messages"
        val latestMessage = messageOrm.findMostRecentMessage(chatId) ?: return "No messages"
        val oldestMessage = messageOrm.findOldestMessage(chatId) ?: return "No messages"
        val beforeMessageId = (viewLastMessage.messageId + 1 + amount)
            .coerceAtMost(latestMessage.messageId + 1)
            .coerceAtLeast(oldestMessage.messageId + MAX_MESSAGES_PER_VIEW + 1)
        val newMessagesView = messageOrm.findBeforeMessageIdOrderedByMessageIdDesc(
            chatId,
            beforeMessageId,
            MAX_MESSAGES_PER_VIEW
        ).reversed()
        val newViewLastMessage = newMessagesView.lastOrNull() ?: return "No messages"
        val newerMessagesRemaining = messageOrm.countMessagesAfterId(chatId, newViewLastMessage.messageId)
        viewState = viewState.copy(
            messagesView = newMessagesView,
            laterMessagesRemaining = newerMessagesRemaining
        )
        setLastReadMessageId(newViewLastMessage.messageId)
        autoscrollToEnd = viewLastMessage.messageId == latestMessage.messageId
        return "ok"
    }

    @Transactional
    @AgentToolMethod(description = "Skip the whole chat and scroll to the very bottom")
    open fun scrollToBottom(): String {
        val chatId = viewState.openedChat?.id ?: return "Chat not opened"
        val latestMessage = messageOrm.findMostRecentMessage(chatId) ?: return "No messages"
        val newMessagesView = messageOrm.findBeforeMessageIdOrderedByMessageIdDesc(
            chatId,
            latestMessage.messageId + 1,
            MAX_MESSAGES_PER_VIEW
        ).reversed()
        viewState = viewState.copy(
            messagesView = newMessagesView,
            laterMessagesRemaining = 0
        )
        setLastReadMessageId(latestMessage.messageId)
        autoscrollToEnd = true
        return "ok"
    }

    @Transactional
    @AgentToolMethod
    open suspend fun sendSticker(fileId: String): String {
        val openedChat = viewState.openedChat ?: return "Chat not opened"
        telegram.sendSticker(openedChat.id, fileId)
        scrollToBottom()
        return "sent"
    }

    @Transactional
    @AgentToolMethod(
        description = "Send message. " +
                "Supported HTML tags: b, i, u, s, tg-spoiler, a[href], tg-emoji[emoji-id], code, pre, blockquote, " +
                "blockquote[expandable]. Message appears in chat immediately after sending."
    )
    open suspend fun send(
        message: String,
        @AgentToolParameter(description = "id of message to reply to") replyTo: Int? = null
    ): String {
        val openedChat = viewState.openedChat ?: return "Chat not opened"
        telegram.sendMessage(openedChat.id, message, replyToMessageId = replyTo)
        scrollToBottom()
        viewState = viewState.copy(oldLastReadMessageId = viewState.openedChat?.metadata?.lastReadMessageId)
        return "sent"
    }

    override suspend fun onOpened() {
        showChats(0)
    }

    override suspend fun onClose() {
        val openedChat = viewState.openedChat
        if (openedChat != null) {
            telegramNotificationService.onChatClosedInAgentApp(openedChat.id)
        }
        scope.cancel("App is closing")
    }

    override fun getAvailableAgentToolMethods(): List<Function<*>> {
        val result = mutableListOf<Function<*>>(::openChat, ::openChatByUsername)
        if (viewState.openedChat != null) {
            result.add(::send)
            result.add(::chatList)
            result.add(::sendSticker)
            result.add(::scroll)
            if (!autoscrollToEnd) {
                result.add(::scrollToBottom)
            }
        }
        return result
    }

    private fun updateLaterMessagesRemaining() {
        val openedChat = viewState.openedChat ?: return
        val chatId = openedChat.id
        val lastMessage = viewState.messagesView.lastOrNull() ?: return
        viewState = viewState.copy(
            laterMessagesRemaining = messageOrm.countMessagesAfterId(chatId, lastMessage.messageId),
            laterNewMessagesRemaining = messageOrm.countMessagesAfterId(
                chatId,
                max(lastMessage.messageId, openedChat.metadata.lastReadMessageId ?: 0)
            )
        )
    }

    private fun chatContainsUnreadMessages(): Boolean {
        val openedChat = viewState.openedChat ?: return false
        val lastReadMessageId = openedChat.metadata.lastReadMessageId
        return messageOrm.countMessagesAfterId(openedChat.id, lastReadMessageId ?: 0) > 0
    }

    private fun viewContainsUnreadMessages(): Boolean {
        val openedChat = viewState.openedChat ?: return false
        val viewLastMessageId = viewState.messagesView.lastOrNull()?.messageId ?: return false
        val lastReadMessageId = openedChat.metadata.lastReadMessageId ?: return true
        return lastReadMessageId < viewLastMessageId
    }

    private fun showChats(pageNumber: Int) {
        val pageIndex = pageNumber - 1
        val chats = chatOrm.findAllEnabled(PageRequest.of(pageIndex, CHATS_PAGE_SIZE))
        val safePageIndex = if (chats.totalPages == 0) 0 else pageIndex.coerceIn(0, chats.totalPages - 1)
        viewState = TelegramAppViewState(
            chatsView = chats.toList(),
            chatsPageIndex = safePageIndex,
            chatsTotalPages = chats.totalPages
        )
    }

    private fun showChat(chat: TelegramChat) {
        val messages: List<TelegramMessage>
        val newerMessagesRemaining: Long

        if (chat.metadata.lastReadMessageId != null) {
            messages = messageOrm.findBeforeMessageIdOrderedByMessageIdDesc(
                chat.id,
                chat.metadata.lastReadMessageId + 1,
                MAX_MESSAGES_PER_VIEW
            ).reversed()
            val newViewLastMessage = messages.lastOrNull() ?: return
            newerMessagesRemaining = messageOrm.countMessagesAfterId(chat.id, newViewLastMessage.messageId)
        } else {
            val firstPage = messageOrm.findFirstOrderedByMessageId(chat.id, MAX_MESSAGES_PER_VIEW)
            messages = firstPage.toList()
            newerMessagesRemaining = firstPage.totalElements - firstPage.numberOfElements
        }

        viewState = viewState.copy(
            openedChat = chat,
            messagesView = messages,
            laterMessagesRemaining = newerMessagesRemaining
        )

        scroll(MAX_MESSAGES_PER_VIEW/2)
    }

    private fun setLastReadMessageId(messageId: Int) {
        val openedChat = viewState.openedChat ?: error("Chat not opened")
        if (openedChat.metadata.lastReadMessageId != null && messageId <= openedChat.metadata.lastReadMessageId) {
            return // Don't move up read cursor
        }
        val newChat = chatOrm.save(openedChat.copy(
            metadata = openedChat.metadata.copy(
                lastReadMessageId = messageId
            )
        ))
        viewState = viewState.copy(
            openedChat = newChat,
            oldLastReadMessageId = viewState.openedChat?.metadata?.lastReadMessageId
        )
    }
}