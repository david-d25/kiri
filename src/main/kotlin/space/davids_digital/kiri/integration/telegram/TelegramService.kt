package space.davids_digital.kiri.integration.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramException
import com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.request.InputMediaDocument
import com.pengrad.telegrambot.model.request.InputMediaPhoto
import com.pengrad.telegrambot.model.request.LabeledPrice
import com.pengrad.telegrambot.model.request.ReplyParameters
import com.pengrad.telegrambot.request.*
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.response.MessagesResponse
import com.pengrad.telegrambot.response.SendResponse
import com.pengrad.telegrambot.utility.kotlin.extension.request.forwardMessage
import com.pengrad.telegrambot.utility.kotlin.extension.request.getChat
import com.pengrad.telegrambot.utility.kotlin.extension.request.getFile
import com.pengrad.telegrambot.utility.kotlin.extension.request.getMe
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import space.davids_digital.kiri.AppProperties
import space.davids_digital.kiri.model.telegram.*
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramMessageOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramUserOrmService
import space.davids_digital.kiri.orm.specifications.telegram.TelegramMessageSpecifications
import space.davids_digital.kiri.service.exception.ServiceException
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

@Service
class TelegramService(
    private val messageOrm: TelegramMessageOrmService,
    private val chatOrm: TelegramChatOrmService,
    private val userOrm: TelegramUserOrmService,
    private val appProperties: AppProperties,
    private val mapper: TelegramIntegrationMapper
) {
    companion object {
        private const val MAX_MESSAGE_LENGTH = 4_096
        private const val MAX_CAPTION_LENGTH = 1_024
        private const val MAX_MEDIA_GROUP_SIZE = 10
        private const val MAX_MESSAGES_PER_SECOND_FREE = 30
        //private const val MAX_MESSAGES_PER_SECOND_PAID = 1000

        const val DONATION_PAYLOAD_PREFIX = "donation:"
        const val DONATION_CURRENCY = "XTR"

        /**
         * Telegram delivers `successful_payment` updates inside the user's PRIVATE chat with the bot, even when
         * the invoice was sent to a group. The original chat is encoded into the payload at send time so we can
         * recover it here for notification routing and admin-UI display.
         */
        fun extractDonationChatId(invoicePayload: String): Long? {
            if (!invoicePayload.startsWith(DONATION_PAYLOAD_PREFIX)) return null
            val rest = invoicePayload.substring(DONATION_PAYLOAD_PREFIX.length)
            val sep = rest.indexOf(':')
            if (sep <= 0) return null
            return rest.substring(0, sep).toLongOrNull()
        }

        const val MAX_DONATION_TITLE_LENGTH = 32
        const val MAX_DONATION_DESCRIPTION_LENGTH = 180
        const val MIN_DONATION_STARS = 1
        const val MAX_DONATION_STARS = 2_500
        const val DONATION_DESCRIPTION_SUFFIX =
            "\n\n— Voluntary donation. No goods or services are provided in return. See /terms."

        private val SUPPORTED_TAGS = setOf(
            "b",
            "i",
            "u",
            "s",
            "tg-spoiler",
            "a",
            "tg-emoji",
            "code",
            "pre",
            "blockquote"
        )
    }

    @Lazy
    @Autowired
    private lateinit var self: TelegramService

    private val log = LoggerFactory.getLogger(this::class.java)

    private lateinit var bot: TelegramBot

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val updatesInternal = MutableSharedFlow<TelegramUpdate>(replay = 0, extraBufferCapacity = 64)
    private val rateLimiter = RateLimiter(MAX_MESSAGES_PER_SECOND_FREE)

    val updates: SharedFlow<TelegramUpdate> = updatesInternal.asSharedFlow()

    private class RateLimiter(private val maxPerSecond: Int) {
        private var sentInWindow = 0
        private var windowStart = System.currentTimeMillis()
        suspend fun acquire() {
            val now = System.currentTimeMillis()
            if (now - windowStart >= 1_000) {
                windowStart = now
                sentInWindow = 0
            }
            if (sentInWindow >= maxPerSecond) {
                delay(1_000 - (now - windowStart))
                windowStart = System.currentTimeMillis()
                sentInWindow = 0
            }
            sentInWindow++
        }
    }

    @PostConstruct
    fun start() {
        bot = TelegramBot(appProperties.integration.telegram.apiKey)
        bot.setUpdatesListener(::processUpdates, ::onBotException)
        updateSelfInfo()
    }

    suspend fun forwardMessage(
        chatId: Long,
        fromChatId: Long,
        messageId: Int,
        disableNotification: Boolean = false
    ) {
        val message = bot.forwardMessage(chatId, fromChatId, messageId) {
            disableNotification(disableNotification)
        }.checkNoErrors("Failed to forward message $messageId from chat $fromChatId to chat $chatId").message()
        messageOrm.save(mapper.toModel(message)!!)
    }

    suspend fun send(chatId: Long, block: TelegramMessageBuilder.() -> Unit): List<TelegramMessage> {
        val builder = TelegramMessageBuilder().apply(block)
        return sendInternal(chatId, builder)
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        images: List<ByteArray> = emptyList(),
        replyMarkup: TelegramInlineKeyboardMarkup? = null,
        disableNotification: Boolean = false,
        messageThreadId: Int? = null,
        replyToMessageId: Int? = null
    ) {
        send(chatId) {
            html(text)
            images.forEach { photo(it) }
            this.replyMarkup = replyMarkup
            this.disableNotification = disableNotification
            this.messageThreadId = messageThreadId
            this.replyToMessageId = replyToMessageId
        }
    }

    suspend fun sendSticker(chatId: Long, fileId: String): TelegramMessage {
        val file = getFile(fileId) ?: throw IllegalArgumentException("File ID $fileId not found")
        return messageOrm.save(
            mapper.toModel(bot.execute(SendSticker(chatId, file.fileUniqueId)).checkNoErrors().message())!!
        )
    }

    suspend fun getStickerSet(name: String): TelegramStickerSet {
        return mapper.toModel(bot.execute(GetStickerSet(name)).checkNoErrors().stickerSet())!!
    }

    private suspend fun sendInternal(chatId: Long, builder: TelegramMessageBuilder): List<TelegramMessage> {
        val sentMessages = mutableListOf<TelegramMessage>()
        val requests = buildSendRequests(
            chatId              = chatId,
            text                = builder.text,
            textEntities        = builder.entities,
            attachments         = builder.attachments,
            replyMarkup         = builder.replyMarkup,
            disableNotification = builder.disableNotification,
            messageThreadId     = builder.messageThreadId,
            replyToMessageId    = builder.replyToMessageId
        )

        for (request in requests) {
            var sent = false
            while (!sent) {
                rateLimiter.acquire()
                val response = bot.execute(request)
                if (response.isOk) {
                    sent = true
                    val messages = extractMessages(response).mapNotNull(mapper::toModel)
                    messages.forEach { messageOrm.save(it) }
                    sentMessages.addAll(messages)
                } else if (response.errorCode() == 429) {
                    val retryAfter = min(response.parameters()?.retryAfter() ?: 1, 1)
                    log.warn("429 received, backing off for {} s", retryAfter)
                    delay(retryAfter.seconds)
                } else {
                    log.error(
                        "Failed to send message or message part to {} ({}): {}",
                        chatId,
                        response.errorCode(),
                        response.description()
                    )
                    throw ServiceException("Failed to send message or message part to $chatId: ${response.description()}")
                }
            }
        }
        return sentMessages
    }

    private fun buildSendRequests(
        chatId: Long,
        text: String,
        textEntities: List<TelegramMessageEntity>,
        attachments: List<TelegramOutgoingAttachment>,
        replyMarkup: TelegramInlineKeyboardMarkup?,
        disableNotification: Boolean,
        messageThreadId: Int?,
        replyToMessageId: Int?
    ): List<BaseRequest<*, *>> {
        val firstLimit = if (attachments.isNotEmpty()) MAX_CAPTION_LENGTH else MAX_MESSAGE_LENGTH
        val slices = splitTextVariableLimit(text, textEntities, firstLimit)
            .ifEmpty { listOf(EntitySlice("", emptyList())) }

        return slices.mapIndexed { index, slice ->
            buildSendRequest(
                chatId              = chatId,
                text                = slice.text,
                textEntities        = slice.entities,
                attachments         = if (index == 0) attachments else emptyList(),
                replyMarkup         = if (index == slices.lastIndex) replyMarkup else null,
                disableNotification = disableNotification,
                messageThreadId     = messageThreadId,
                replyToMessageId    = replyToMessageId
            )
        }
    }

    private fun buildSendRequest(
        chatId: Long,
        text: String,
        textEntities: List<TelegramMessageEntity>,
        attachments: List<TelegramOutgoingAttachment>,
        replyMarkup: TelegramInlineKeyboardMarkup?,
        disableNotification: Boolean,
        messageThreadId: Int?,
        replyToMessageId: Int?
    ): BaseRequest<*, *> {
        require(attachments.size <= MAX_MEDIA_GROUP_SIZE) { "Telegram allows up to $MAX_MEDIA_GROUP_SIZE attachments per message" }

        val hasDocuments = attachments.any { it is TelegramOutgoingAttachment.Document }
        val hasPhotos = attachments.any { it is TelegramOutgoingAttachment.Photo }
        require(!(hasDocuments && hasPhotos)) { "Telegram does not support mixing documents with photos in a single message" }

        val entitiesDto = textEntities.mapNotNull { mapper.toDto(it) }.toTypedArray()
        val replyParams = replyToMessageId?.let { ReplyParameters(it) }
        val replyMarkupDto = replyMarkup?.let { mapper.toDto(it)!! }

        return when {
            attachments.isEmpty() -> SendMessage(chatId, text).apply {
                if (entitiesDto.isNotEmpty()) entities(*entitiesDto)
                replyMarkupDto?.let { replyMarkup(it) }
                disableNotification(disableNotification)
                messageThreadId?.let { messageThreadId(it) }
                replyParams?.let { replyParameters(it) }
            }

            attachments.size == 1 -> when (val att = attachments.single()) {
                is TelegramOutgoingAttachment.Photo -> SendPhoto(chatId, att.data).apply {
                    caption(text)
                    if (entitiesDto.isNotEmpty()) captionEntities(*entitiesDto)
                    replyMarkupDto?.let { replyMarkup(it) }
                    disableNotification(disableNotification)
                    messageThreadId?.let { messageThreadId(it) }
                    replyParams?.let { replyParameters(it) }
                }

                is TelegramOutgoingAttachment.Document -> SendDocument(chatId, att.data).apply {
                    fileName(att.name)
                    caption(text)
                    if (entitiesDto.isNotEmpty()) captionEntities(*entitiesDto)
                    replyMarkupDto?.let { replyMarkup(it) }
                    disableNotification(disableNotification)
                    messageThreadId?.let { messageThreadId(it) }
                    replyParams?.let { replyParameters(it) }
                }
            }

            else -> {
                val media = attachments.mapIndexed { index, att ->
                    when (att) {
                        is TelegramOutgoingAttachment.Photo -> InputMediaPhoto(att.data)
                        is TelegramOutgoingAttachment.Document -> InputMediaDocument(att.data).apply {
                            fileName(att.name)
                        }
                    }.apply {
                        if (index == 0) {
                            caption(text)
                            captionEntities(*entitiesDto)
                        }
                    }
                }
                SendMediaGroup(chatId, *media.toTypedArray()).apply {
                    disableNotification(disableNotification)
                    messageThreadId?.let { messageThreadId(it) }
                    replyParams?.let { replyParameters(it) }
                }
            }
        }
    }

    private fun extractMessages(resp: BaseResponse): List<Message> = when (resp) {
        is SendResponse -> listOfNotNull(resp.message())
        is MessagesResponse -> resp.messages()?.toList() ?: emptyList()
        else -> {
            log.error("Couldn't extract message from response of type ${resp::class}")
            emptyList()
        }
    }

    /**
     * Send a Telegram Stars donation invoice to the chat.
     *
     * The system enforces three guarantees mandated by Telegram's Stars policy and our internal rules:
     *  - Currency is forced to XTR with empty provider token (Stars donation).
     *  - A fixed suffix is appended to the description making it unmistakably clear that the payment is voluntary
     *    and no goods or services are provided in return.
     *  - The payload is always generated server-side with a fixed `donation:` prefix so the pre-checkout handler
     *    can recognize and auto-approve it.
     */
    suspend fun sendDonationInvoice(
        chatId: Long,
        title: String,
        description: String,
        starAmount: Int
    ): TelegramMessage {
        require(starAmount in MIN_DONATION_STARS..MAX_DONATION_STARS) {
            "Donation amount must be between $MIN_DONATION_STARS and $MAX_DONATION_STARS Stars, got $starAmount"
        }
        val trimmedTitle = title.trim()
        require(trimmedTitle.isNotEmpty()) { "Invoice title must not be empty" }
        require(trimmedTitle.length <= MAX_DONATION_TITLE_LENGTH) {
            "Invoice title must be ≤ $MAX_DONATION_TITLE_LENGTH characters, got ${trimmedTitle.length}"
        }
        val trimmedDescription = description.trim()
        require(trimmedDescription.isNotEmpty()) { "Invoice description must not be empty" }
        require(trimmedDescription.length <= MAX_DONATION_DESCRIPTION_LENGTH) {
            "Invoice description must be ≤ $MAX_DONATION_DESCRIPTION_LENGTH characters, got ${trimmedDescription.length}"
        }
        val titleMatchesDonationKeyword = Regex("(?i)donat|support|tip|contribut").containsMatchIn(trimmedTitle)
        if (!titleMatchesDonationKeyword) {
            log.warn(
                "Donation invoice title '{}' does not contain a donation-related keyword; consider rewording.",
                trimmedTitle
            )
        }

        val payload = "$DONATION_PAYLOAD_PREFIX$chatId:${UUID.randomUUID()}"
        val fullDescription = trimmedDescription + DONATION_DESCRIPTION_SUFFIX
        val request = SendInvoice(
            chatId,
            trimmedTitle,
            fullDescription,
            payload,
            DONATION_CURRENCY,
            listOf(LabeledPrice("Stars", starAmount))
        )
        rateLimiter.acquire()
        val sent = bot.execute(request)
            .checkNoErrors("Failed to send donation invoice to chat $chatId")
            .message()
        val model = mapper.toModel(sent) ?: error("Mapper returned null for sent invoice in chat $chatId")
        val invoice = model.invoice
            ?: error("Sent donation invoice message has no invoice field; chat=$chatId, messageId=${model.messageId}")
        val withPayload = model.copy(invoice = invoice.copy(payload = payload))
        return messageOrm.save(withPayload)
    }

    /**
     * Counts donation invoices issued by the bot itself in [chatId] within the last [withinHours] hours.
     * Used to throttle how often the agent can re-issue donation invoices in the same chat.
     */
    suspend fun countRecentDonationInvoices(chatId: Long, withinHours: Long): Long {
        val botId = appProperties.integration.telegram.botId
        val since = OffsetDateTime.now().minusHours(withinHours)
        val spec = TelegramMessageSpecifications.chatId(chatId)
            .and(TelegramMessageSpecifications.fromUser(botId))
            .and(TelegramMessageSpecifications.dateAfter(since))
            .and(TelegramMessageSpecifications.invoiceNotNull())
        return withContext(Dispatchers.IO) { messageOrm.count(spec) }
    }

    /**
     * Confirms or rejects a Telegram pre-checkout query. Telegram requires this answer within 10 seconds,
     * otherwise the payment is automatically cancelled.
     */
    suspend fun answerPreCheckoutQuery(id: String, ok: Boolean = true, errorMessage: String? = null) {
        val request = if (ok) {
            AnswerPreCheckoutQuery(id)
        } else {
            AnswerPreCheckoutQuery(id, errorMessage ?: "Payment was rejected")
        }
        bot.execute(request).checkNoErrors("Failed to answer pre-checkout query $id")
    }

    /**
     * Refunds a successful Telegram Stars payment. Available for 21 days after payment per Telegram's policy.
     */
    suspend fun refundStarPayment(userId: Long, telegramPaymentChargeId: String) {
        bot.execute(RefundStarPayment(userId, telegramPaymentChargeId))
            .checkNoErrors("Failed to refund Stars payment $telegramPaymentChargeId for user $userId")
    }

    private suspend fun handlePreCheckoutQuery(query: TelegramPreCheckoutQuery) {
        try {
            val isDonationPayload = query.invoicePayload.startsWith(DONATION_PAYLOAD_PREFIX)
            val isStarsCurrency = query.currency == DONATION_CURRENCY
            val invoiceKnown: Boolean
            val alreadyPaid: Boolean
            if (isDonationPayload) {
                val (known, paid) = withContext(Dispatchers.IO) {
                    val k = messageOrm.count(
                        TelegramMessageSpecifications.invoicePayloadEquals(query.invoicePayload)
                    ) > 0
                    val p = messageOrm.count(
                        TelegramMessageSpecifications.successfulPaymentInvoicePayloadEquals(query.invoicePayload)
                    ) > 0
                    k to p
                }
                invoiceKnown = known
                alreadyPaid = paid
            } else {
                invoiceKnown = false
                alreadyPaid = false
            }
            if (isDonationPayload && isStarsCurrency && invoiceKnown && !alreadyPaid) {
                answerPreCheckoutQuery(query.id, ok = true)
                log.info(
                    "Approved pre-checkout query {} for {} {} (payload: {})",
                    query.id, query.totalAmount, query.currency, query.invoicePayload
                )
            } else {
                log.warn(
                    "Rejecting pre-checkout query {}: payload='{}', currency='{}', invoiceKnown={}, alreadyPaid={}",
                    query.id, query.invoicePayload, query.currency, invoiceKnown, alreadyPaid
                )
                answerPreCheckoutQuery(
                    query.id,
                    ok = false,
                    errorMessage = "This invoice is no longer valid. Please contact support."
                )
            }
        } catch (e: Exception) {
            log.error("Failed to handle pre-checkout query ${query.id}", e)
        }
    }

    suspend fun deleteMessage(chatId: Long, messageId: Int) {
        val response = bot.execute(DeleteMessage(chatId, messageId))
        if (!response.isOk) {
            val description = response.description() ?: ""
            if (response.errorCode() == 400 &&
                (description.contains("message to delete not found") ||
                        description.contains("message can't be deleted"))
            ) {
                log.debug("Message $messageId in chat $chatId could not be deleted: $description")
                return
            }
            response.checkNoErrors("Failed to delete message $messageId in chat $chatId")
        }
    }

    private suspend fun cleanupDonationInvoiceOnPayment(message: TelegramMessage) {
        val payment = message.successfulPayment ?: return
        val invoiceMessage = withContext(Dispatchers.IO) {
            messageOrm.search(
                TelegramMessageSpecifications.invoicePayloadEquals(payment.invoicePayload),
                PageRequest.of(0, 1)
            ).content.firstOrNull()
        } ?: return
        val isGroup = withContext(Dispatchers.IO) {
            chatOrm.findById(invoiceMessage.chatId)
        }?.let { it.type != TelegramChat.Type.PRIVATE } ?: false
        if (isGroup) {
            // Keep the invoice message visible as a record of the donation, but strip the Pay button so
            // other group members can't keep clicking it (each click after the first would just produce
            // an "already paid" rejection from our pre-checkout handler). The agent will follow up with
            // a thank-you message that serves as the visible "paid" status.
            try {
                clearMessageReplyMarkup(invoiceMessage.chatId, invoiceMessage.messageId)
                return
            } catch (e: Exception) {
                log.warn(
                    "Failed to clear donation invoice buttons on message ${invoiceMessage.messageId} " +
                            "in chat ${invoiceMessage.chatId}; falling back to delete",
                    e
                )
            }
        }
        try {
            deleteMessage(invoiceMessage.chatId, invoiceMessage.messageId)
        } catch (e: Exception) {
            log.warn(
                "Failed to delete donation invoice message ${invoiceMessage.messageId} " +
                        "in chat ${invoiceMessage.chatId} after payment",
                e
            )
        }
    }

    private suspend fun clearMessageReplyMarkup(chatId: Long, messageId: Int) {
        // Calling editMessageReplyMarkup without `reply_markup` removes the inline keyboard. For invoice
        // messages this strips the auto-generated "Pay X" button.
        val response = bot.execute(EditMessageReplyMarkup(chatId, messageId))
        if (!response.isOk) {
            val description = response.description() ?: ""
            if (response.errorCode() == 400 &&
                (description.contains("message to edit not found") ||
                        description.contains("message can't be edited") ||
                        description.contains("exactly the same"))
            ) {
                log.debug("Reply markup of message $messageId in chat $chatId not edited: $description")
                return
            }
            response.checkNoErrors("Failed to clear reply markup of message $messageId in chat $chatId")
        }
    }

    suspend fun editMessage(
        chatId: Long,
        messageId: Int,
        text: String,
        replyMarkup: TelegramInlineKeyboardMarkup? = null
    ) {
        val response = bot.execute(EditMessageText(chatId, messageId, text).apply {
            replyMarkup?.let { replyMarkup(mapper.toDto(it)!!) }
        })
        if (response.errorCode() == 400 && response.description().contains("exactly the same")) {
            log.debug(
                "Message $messageId in chat $chatId was not edited because the text and reply markup are the same " +
                        "as before."
            )
            return
        }
        response.checkNoErrors("Failed to edit message $messageId in chat $chatId")
    }

    @Cacheable(value = ["TelegramService#fetchAndSaveChatById"], key = "#chatId", sync = true)
    suspend fun fetchAndSaveChatById(chatId: Long): TelegramChat? {
        val response = bot.getChat(chatId)
        if (response.errorCode() == 400) {
            log.error("Failed to get chat with id $chatId: ${response.description()}")
            return null
        }
        response.checkNoErrors("Failed to get chat id $chatId")
        return chatOrm.save(mapper.toModel(response.chat())!!)
    }

    @Cacheable(value = ["TelegramService#fetchAndSaveChatByUsername"], key = "#username", sync = true)
    suspend fun fetchAndSaveChatByUsername(username: String): TelegramChat? {
        val response = bot.getChat("@$username")
        if (response.errorCode() == 400) {
            log.error("Failed to get chat with username '$username': ${response.description()}")
            return null
        }
        response.checkNoErrors("Failed to get chat by username '$username'")
        return chatOrm.save(mapper.toModel(response.chat())!!)
    }

    suspend fun answerCallbackQuery(callbackQueryId: String, text: String? = null) {
        bot.execute(AnswerCallbackQuery(callbackQueryId).apply {
            text?.let { text(it) }
        }).checkNoErrors("Failed to answer callback query")
    }

    fun createMessageLink(chatId: Long, messageId: Int): String {
        val internalChatId = chatId.toString().removePrefix("-100").removePrefix("-")
        val defaultFallback = "https://t.me/c/$internalChatId/$messageId"
        val message = messageOrm.find(chatId, messageId)
        val chat = chatOrm.findById(chatId)
        val threadId = message?.messageThreadId
        val chatUsername = chat?.username?.removePrefix("@")
        if (!chatUsername.isNullOrBlank()) {
            return if (threadId != null) {
                "https://t.me/${chatUsername}/$threadId/$messageId"
            } else {
                "https://t.me/${chatUsername}/$messageId"
            }
        }
        return if (threadId != null) {
            "https://t.me/c/$internalChatId/$threadId/$messageId"
        } else {
            defaultFallback
        }
    }

    fun createMessageLink(message: TelegramMessage): String {
        return createMessageLink(message.chatId, message.messageId)
    }

    suspend fun chatExists(id: Long): Boolean {
        if (chatOrm.existsById(id)) {
            return true
        }
        try {
            val response = bot.getChat(id).checkNoErrors().chat()
            chatOrm.save(mapper.toModel(response)!!)
            return true
        } catch (e: Exception) {
            log.info("Could not fetch chat with id $id (${e.message}), will assume it doesn't exist")
            return false
        }
    }

    @Cacheable(value = ["TelegramService#getUser"], key = "#id")
    fun getUser(id: Long): TelegramUser? {
        return userOrm.findById(id)
    }

    @Cacheable(value = ["TelegramService#getUserByUsername"], key = "#username")
    fun getUserByUsername(username: String): TelegramUser? {
        return userOrm.findByUsername(username)
    }

    @Cacheable(value = ["TelegramService#getFileContent"], key = "#fileId")
    suspend fun getFileContent(fileId: String): ByteArray {
        return bot.getFileContent(
            bot.getFile(fileId).checkNoErrors().file()
        )
    }

    suspend fun getFile(fileId: String): TelegramFile? {
        return mapper.toModel(bot.getFile(fileId).checkNoErrors().file())
    }

    private suspend fun onMessage(message: TelegramMessage) {
        log.debug("Received Telegram message from chat {}", message.chatId)
        try {
            withContext(Dispatchers.IO) {
                messageOrm.save(message)
            }
        } catch (e: Exception) {
            log.error("Failed to save Telegram message", e)
        }
    }

    private fun refreshUser(user: User) {
        userOrm.save(mapper.toModel(user))
    }

    private fun refreshChat(chat: Chat) {
        val id = chat.id()
        try {
            if (!chatOrm.existsById(id)) {
                log.debug("Chat with id $id does not exist, fetching and saving")
                val chat = bot.getChat(id)
                    .checkNoErrors("Failed to get Telegram chat with id $id")
                    .chat()
                    .let(mapper::toModel)!!
                chatOrm.save(chat)
            }
        } catch (e: Exception) {
            log.error("Failed to get and save Telegram chat", e)
        }
    }

    fun getSelf(): TelegramUser {
        return self.getUser(appProperties.integration.telegram.botId)
            ?: bot.getMe().checkNoErrors().user().let(mapper::toModel)
    }

    private fun updateSelfInfo() {
        try {
            userOrm.save(bot.getMe().checkNoErrors().user().let(mapper::toModel))
        } catch (e: Exception) {
            log.error("Failed to get and save bot info", e)
        }
    }

    private fun processUpdates(updates: List<Update>): Int {
        serviceScope.launch {
            updates.forEach { update ->
                val updateModel = try {
                    mapper.toModel(update)!!
                } catch (e: Exception) {
                    log.error("Failed to create update model, it will be skipped", e)
                    return@forEach
                }
                if (updateModel.message != null) {
                    onMessage(updateModel.message)
                }
                if (update.message() != null) {
                    if (update.message().from() != null) {
                        refreshUser(update.message().from())
                    }
                }
                if (updateModel.preCheckoutQuery != null) {
                    handlePreCheckoutQuery(updateModel.preCheckoutQuery)
                }
                if (updateModel.message?.successfulPayment != null) {
                    cleanupDonationInvoiceOnPayment(updateModel.message)
                }
                updatesInternal.emit(updateModel)
            }
        }
        return CONFIRMED_UPDATES_ALL
    }

    private fun onBotException(e: TelegramException) {
        if (e.response() != null) {
            log.error("Telegram API error ({}): {}", e.response().errorCode(), e.response().description(), e)
        } else {
            log.error("Telegram API error", e)
        }
    }

    private fun <T : BaseResponse> T.checkNoErrors(customMessage: String? = null): T {
        if (isOk) {
            return this
        }
        log.error("Telegram API error ({}): {}", errorCode(), description())
        if (customMessage != null) {
            throw ServiceException(customMessage + ": " + description())
        } else {
            throw ServiceException("Telegram API error (${errorCode()}): ${description()}")
        }
    }

    fun splitTextVariableLimit(
        fullText: String,
        entities: List<TelegramMessageEntity>,
        firstLimit: Int,
        otherLimit: Int = MAX_MESSAGE_LENGTH,  // 4096
    ): List<EntitySlice> {

        require(firstLimit in 1..otherLimit)
        require(entities.none { it.length > otherLimit }) {
            "Entity length exceeds Telegram hard limit ($otherLimit)"
        }

        val out = mutableListOf<EntitySlice>()
        var chunkStart = 0
        var isFirst = true

        while (chunkStart < fullText.length) {
            val limit = if (isFirst) firstLimit else otherLimit
            isFirst = false

            val hardEnd  = (chunkStart + limit).coerceAtMost(fullText.length)
            var chunkEnd = hardEnd

            val hitLimit = hardEnd == chunkStart + limit && hardEnd < fullText.length
            if (hitLimit) {
                entities.forEach { e ->
                    val eStart = e.offset
                    val eEnd   = e.offset + e.length
                    if (chunkEnd in (eStart + 1)..<eEnd) {
                        chunkEnd = eStart
                    }
                }

                var optEnd = chunkEnd
                while (optEnd > chunkStart && !fullText[optEnd - 1].isWhitespace()) optEnd--
                if (optEnd - chunkStart >= 5) chunkEnd = optEnd
            }

            val partEntities = entities
                .filter { it.offset < chunkEnd && it.offset + it.length > chunkStart }
                .map { e ->
                    val start = (e.offset - chunkStart).coerceAtLeast(0)
                    val end   = (e.offset + e.length).coerceAtMost(chunkEnd) - chunkStart
                    e.copy(offset = start, length = end - start)
                }

            out += EntitySlice(fullText.substring(chunkStart, chunkEnd), partEntities)
            chunkStart = chunkEnd
        }
        return out
    }

    data class EntitySlice(
        val text: String,
        val entities: List<TelegramMessageEntity>
    )
}