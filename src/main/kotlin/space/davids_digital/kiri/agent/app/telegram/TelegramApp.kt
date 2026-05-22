package space.davids_digital.kiri.agent.app.telegram

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.engine.lifecycle.OnAgentWake
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.DataFrameUtils.PRETTY_DATE_TIME_PATTERN
import space.davids_digital.kiri.agent.frame.DataFrameUtils.fromPrettyStringToZonedDateTime
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.frame.StaticDataFrame
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.frame.trackToolCall
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageEntity
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramMessageOrmService
import space.davids_digital.kiri.orm.specifications.telegram.TelegramMessageSpecifications
import space.davids_digital.kiri.orm.service.SettingOrmService
import space.davids_digital.kiri.service.TelegramDonationService
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
    private val telegramDonationService: TelegramDonationService,
    private val chatOrm: TelegramChatOrmService,
    private val messageOrm: TelegramMessageOrmService,
    private val files: TemporaryFilesService,
    settings: SettingOrmService
): AgentApp("telegram") {

    companion object {
        private const val CHATS_PAGE_SIZE = 10
        private const val MAX_MESSAGES_PER_VIEW = 10
    }

    private val log = LoggerFactory.getLogger(javaClass)

    private val autoSwitchOnWake by settings.declareBoolean("apps.telegram.autoSwitchOnWake", true)
    private val minHoursBetweenDonationInvoicesPerChat by settings.declareLong(
        "payments.minHoursBetweenInvoicesPerChat",
        24
    )

    private var selectedChatId: Long? = null

    override fun getAvailableAgentToolMethods(): List<KFunction<*>> = buildList {
        add(::getUserProfile)
        add(::listChats)
        add(::switchToChatById)
        add(::switchToChatByUsername)
        if (selectedChatId != null) {
            add(::getCurrentChat)
            add(::listLatestMessages)
            add(::searchMessages)
            add(::send)
            add(::sendSticker)
            add(::closeChat)
            add(::getChatInfo)
            add(::download)
            add(::sendDonationInvoice)
            add(::refundDonation)
        }
    }

    @AgentToolMethod(description = "Return the currently selected chat title, id, and unread message count")
    suspend fun getCurrentChat(): String {
        val chatId = selectedChatId ?: return "No chat is currently selected."
        val chat = withContext(Dispatchers.IO) { chatOrm.findById(chatId) }
        val title = chat?.title ?: chat?.firstName ?: chatId.toString()
        val unreadCount = withContext(Dispatchers.IO) {
            messageOrm.countMessagesAfterId(chatId, chat?.metadata?.lastReadMessageId ?: 0)
        }
        return "Selected chat: $title (id $chatId). Unread messages: $unreadCount"
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
        val sentMessage = telegram.sendSticker(selectedChatId, fileId)
        advanceReadPointerIfSafe(selectedChatId, listOf(sentMessage.messageId))
        return "sent"
    }

    @AgentToolMethod(
        description = "Send a Telegram Stars donation invoice to the current chat. " +
                "Use ONLY when contextually appropriate (e.g. user expresses gratitude or asks how to support). " +
                "NEVER pressure the user. If declined or ignored, do NOT retry. " +
                "A fixed disclaimer about voluntariness and refunds is appended automatically — do not duplicate it."
    )
    suspend fun sendDonationInvoice(
        @AgentToolParameter(description = "amount of Telegram Stars (XTR), 1..2500")
        starAmount: Int,
        @AgentToolParameter(
            description = "invoice title shown on the card and confirmation modal " +
                    "(≤32 chars, should mention donation/support, e.g. 'Support the bot')"
        )
        title: String,
        @AgentToolParameter(
            description = "short description (≤180 chars). System auto-appends a voluntary-donation disclaimer."
        )
        description: String
    ): String {
        val chatId = selectedChatId ?: return "Chat not opened"
        val cooldownHours = minHoursBetweenDonationInvoicesPerChat
        if (cooldownHours > 0) {
            val recent = telegram.countRecentDonationInvoices(chatId, cooldownHours)
            if (recent > 0) {
                return "Cooldown active: a donation invoice was already sent to this chat within the last " +
                        "$cooldownHours hour(s). Do not retry."
            }
        }
        return try {
            val sent = telegram.sendDonationInvoice(chatId, title, description, starAmount)
            advanceReadPointerIfSafe(chatId, listOf(sent.messageId))
            "Donation invoice sent (${starAmount}⭐)"
        } catch (e: IllegalArgumentException) {
            "Invalid invoice arguments: ${e.message}"
        }
    }

    @AgentToolMethod(
        description = "Refund a Telegram Stars donation made in the currently selected chat. " +
                "Use only if the user clearly asks for a refund or if the payment was a clear mistake. " +
                "Available for 21 days after the original payment."
    )
    suspend fun refundDonation(
        @AgentToolParameter(description = "telegramPaymentChargeId from the successful_payment of the original message")
        telegramPaymentChargeId: String
    ): String {
        val chatId = selectedChatId ?: return "Chat not opened"
        val donation = withContext(Dispatchers.IO) {
            telegramDonationService.findDonationByChargeId(telegramPaymentChargeId)
        } ?: return "No donation found with charge id $telegramPaymentChargeId"
        if (donation.chatId != chatId) {
            return "Donation $telegramPaymentChargeId belongs to a different chat — refunds are scoped to the open chat."
        }
        val userId = donation.fromId
            ?: return "Donation $telegramPaymentChargeId has no associated user — cannot refund."
        return try {
            telegram.refundStarPayment(userId, telegramPaymentChargeId)
            "Refund issued for charge $telegramPaymentChargeId"
        } catch (e: Exception) {
            log.error("Refund failed for charge $telegramPaymentChargeId", e)
            "Refund failed: ${e.message}"
        }
    }

    /**
     * Send a message with optional photo and/or document attachments.
     * Note: Telegram does not allow mixing photos and documents in a single message —
     * use either [images] or [documents], not both.
     */
    @AgentToolMethod(
        description = "Send message with optional attachments. " +
                "Note: images and documents cannot be mixed in a single message."
    )
    suspend fun send(
        @AgentToolParameter(
            description = "message text; " +
                    "Supported HTML tags: b, i, u, s, tg-spoiler, a[href], tg-emoji[emoji-id], code, pre, " +
                    "blockquote, blockquote[expandable]."
        )
        message: String,

        @AgentToolParameter(description = "id of message to reply to")
        replyTo: Int? = null,

        @AgentToolParameter(description = "photo filenames from temporary files; photos are compressed as JPEG")
        images: List<String> = emptyList(),

        @AgentToolParameter(description = "document filenames from temporary files (sent as-is, i.e. photos without compression)")
        documents: List<String> = emptyList()
    ): String {
        val selectedChatId = selectedChatId ?: return "Chat not opened"
        val imageContents = images.map { files.getContent(it) ?: error("file '$it' not found") }
        val documentContents = documents.map { it to (files.getContent(it) ?: error("file '$it' not found")) }
        val sentMessages = telegram.send(selectedChatId) {
            html(message)
            imageContents.forEach { photo(it) }
            documentContents.forEach { (name, data) -> document(data, name) }
            replyToMessageId = replyTo
        }
        advanceReadPointerIfSafe(selectedChatId, sentMessages.map { it.messageId })
        return "sent"
    }

    @OnAgentWake
    suspend fun autoSwitchOnNotification(frames: FrameBuffer) {
        if (!autoSwitchOnWake) return

        // Pick the most recent telegram notification — earlier ones remain for the LLM to handle
        val chatId = frames.toList().asReversed()
            .filterIsInstance<StaticDataFrame>()
            .firstOrNull { it.tag == "notification" && it.attributes["app"] == "telegram" }
            ?.attributes?.get("chatId")?.toLongOrNull() ?: return

        log.info("Auto-switching to chat {} on wake", chatId)
        frames.trackToolCall(this::switchToChatById, chatId)

        // Auto-list unread messages if the count fits in a single view
        val unreadCount = withContext(Dispatchers.IO) {
            val chat = chatOrm.findById(chatId) ?: return@withContext 0L
            messageOrm.countMessagesAfterId(chatId, chat.metadata.lastReadMessageId ?: 0)
        }
        if (unreadCount in 1..<MAX_MESSAGES_PER_VIEW) {
            log.info("Auto-listing {} unread messages in chat {}", unreadCount, chatId)
            frames.trackToolCall(this::listLatestMessages, 0, false)
        }
    }

    override suspend fun onClose() {
        val selectedChatId = selectedChatId
        if (selectedChatId != null) {
            telegramNotificationService.onChatClosedInAgentApp(selectedChatId)
        }
    }

    /**
     * After sending messages, advances the read pointer only if the ONLY unread messages
     * are the ones we just sent. If someone else sent a message in between, the pointer
     * is NOT advanced so the agent sees that message on the next listing.
     */
    private suspend fun advanceReadPointerIfSafe(chatId: Long, sentMessageIds: List<Int>) {
        if (sentMessageIds.isEmpty()) return
        val maxSentId = sentMessageIds.max()
        withContext(Dispatchers.IO) {
            val chat = chatOrm.findById(chatId) ?: return@withContext
            val lastReadId = chat.metadata.lastReadMessageId ?: 0
            val totalUnread = messageOrm.countMessagesAfterId(chatId, lastReadId)
            if (totalUnread == sentMessageIds.size.toLong()) {
                setLastReadMessageId(chatId, maxSentId)
            }
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