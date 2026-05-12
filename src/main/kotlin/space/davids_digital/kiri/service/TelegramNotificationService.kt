package space.davids_digital.kiri.service

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.notification.Notification
import space.davids_digital.kiri.agent.notification.NotificationManager
import space.davids_digital.kiri.bot.telegram.command.CommandUtils.isCommand
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.model.telegram.TelegramUpdate
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.service.SettingOrmService
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentSkipListSet
import javax.annotation.PostConstruct

@Service
class TelegramNotificationService (
    private val telegram: TelegramService,
    private val notificationManager: NotificationManager,
    settings: SettingOrmService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val chatsOpenedInAgentApp = ConcurrentSkipListSet<Long>()

    private val respondToKiriPrefix by settings.declareBoolean("apps.telegram.respondToKiriPrefix", false)
    private val agentEnabled by settings.declareBoolean("agent.enabled", true)
    private val agentDisabledMessage by settings.declareString(
        "agent.disabledMessage",
        "Agent is disabled, please reach to admin"
    )

    @PostConstruct
    private fun init() {
        startUpdateProcessing()
    }

    @PreDestroy
    private fun destroy() {
        scope.cancel()
    }

    fun onChatOpenedInAgentApp(chatId: Long) {
        chatsOpenedInAgentApp.add(chatId)
    }

    fun onChatClosedInAgentApp(chatId: Long) {
        chatsOpenedInAgentApp.remove(chatId)
    }

    private fun startUpdateProcessing() {
        scope.launch {
            telegram.updates.collect { update ->
                try {
                    onUpdate(update)
                } catch (e: Exception) {
                    log.error("Error handling update", e)
                }
            }
        }
    }

    private suspend fun onUpdate(update: TelegramUpdate) {
        if (update.message?.successfulPayment != null) {
            onSuccessfulPayment(update.message)
            return
        }
        if (update.message?.refundedPayment != null) {
            // Refunds are issued either by the agent itself (via tool / admin UI) or by the user
            // through Telegram's built-in receipt; in both cases the agent does not need a wake-up.
            return
        }
        when {
            update.message != null -> onMessage(update.message)
        }
    }

    private suspend fun onSuccessfulPayment(message: TelegramMessage) {
        val payment = message.successfulPayment ?: return
        // Telegram delivers successful_payment in the user's PRIVATE chat with the bot, even when the invoice
        // was sent to a group. Recover the original chat from the payload so the agent wakes up in the right
        // place (and the chat label in the notification text matches reality).
        val originalChatId = TelegramService.extractDonationChatId(payment.invoicePayload) ?: message.chatId
        val chat = telegram.fetchAndSaveChatById(originalChatId) ?: return
        val user = message.fromId?.let { telegram.getUser(it) }
        val payer = when {
            user != null -> formatUser(user, message.fromId)
            chat.type == TelegramChat.Type.PRIVATE -> formatPrivateChatUser(chat)
            else -> formatUser(null, message.fromId)
        }
        val starSymbol = if (payment.currency == "XTR") "⭐" else payment.currency
        val location = if (chat.type == TelegramChat.Type.PRIVATE) "" else " in ${formatChat(chat)}"
        sendNotification(
            "$payer sent ${payment.totalAmount} $starSymbol donation$location. " +
                    "Charge id: ${payment.telegramPaymentChargeId}",
            originalChatId
        )
    }

    private suspend fun onMessage(message: TelegramMessage) {
        val chat = telegram.fetchAndSaveChatById(message.chatId)
        if (chat == null) {
            log.warn("Received message from unknown chat with id {}", message.chatId)
            return
        }
        val chatDisabledMessage = "Agent is disabled in this chat. Please talk to the administrator to enable it."
        val chatEnabled = chat.metadata.enabled
        val self = telegram.getSelf()
        val textOrCaption = message.text ?: message.caption
        if (textOrCaption != null && isCommand(textOrCaption, chat.type, telegram.getSelf().username ?: "")) {
            return
        }
        val isAgentMentioned = textOrCaption?.contains("@" + self.username) == true
        val isPrivateChat = chat.type == TelegramChat.Type.PRIVATE
        val isAgentMessageRepliedTo = message.replyToMessage?.fromId == self.id
        val isKiriPrefixed = respondToKiriPrefix && textOrCaption?.startsWithKiriPrefix() == true
        val chatIsOpenedInApp = message.chatId in chatsOpenedInAgentApp
        if (!agentEnabled) {
            if (isPrivateChat || isAgentMentioned || isAgentMessageRepliedTo || isKiriPrefixed) {
                telegram.sendMessage(message.chatId, agentDisabledMessage)
            }
            return
        }
        if (chatIsOpenedInApp) {
            // Current chat is opened in the agent app, just wake up the agent
            if (isPrivateChat || isAgentMentioned || isAgentMessageRepliedTo || isKiriPrefixed) {
                sendNotification("New message in current chat", message.chatId)
                return
            }
        }
        if (isPrivateChat) {
            if (!chatEnabled) {
                telegram.sendMessage(message.chatId, chatDisabledMessage)
            } else {
                sendNotification("New message in private chat with ${formatPrivateChatUser(chat)}", message.chatId)
            }
            return
        }
        val user = message.fromId?.let { telegram.getUser(it) }
        val chatLabel = formatChat(chat)
        val userLabel = formatUser(user, message.fromId)
        if (isAgentMentioned) {
            if (!chatEnabled) {
                telegram.sendMessage(message.chatId, chatDisabledMessage)
            } else {
                sendNotification("$userLabel mentioned you in chat $chatLabel", message.chatId)
            }
            return
        }
        if (isAgentMessageRepliedTo) {
            if (!chatEnabled) {
                telegram.sendMessage(message.chatId, chatDisabledMessage)
            } else {
                sendNotification("$userLabel replied to you in chat $chatLabel", message.chatId)
            }
            return
        }
        if (isKiriPrefixed) {
            if (!chatEnabled) {
                telegram.sendMessage(message.chatId, chatDisabledMessage)
            } else {
                sendNotification("$userLabel addressed you by name in chat $chatLabel", message.chatId)
            }
            return
        }
    }

    private fun String.startsWithKiriPrefix(): Boolean {
        val trimmed = trimStart()
        return trimmed.startsWith("кири", ignoreCase = true) || trimmed.startsWith("kiri", ignoreCase = true)
    }

    private fun formatUser(user: TelegramUser?, fallbackId: Long?): String {
        if (user == null) {
            return if (fallbackId != null) "user (id $fallbackId)" else "unknown user"
        }
        val handle = user.username?.let { "@$it" } ?: user.firstName
        return "$handle (id ${user.id})"
    }

    private fun formatChat(chat: TelegramChat): String {
        val handle = chat.username?.let { "@$it" } ?: chat.title ?: chat.firstName ?: "chat"
        return "$handle (id ${chat.id})"
    }

    private fun formatPrivateChatUser(chat: TelegramChat): String {
        val handle = chat.username?.let { "@$it" } ?: chat.firstName ?: chat.title ?: "user"
        return "$handle (id ${chat.id})"
    }

    private suspend fun sendNotification(text: String, chatId: Long) {
        notificationManager.push(Notification(
            sentAt = ZonedDateTime.now(),
            metadata = mapOf(
                "app" to "telegram",
                "chatId" to chatId.toString()
            ),
            content = dataFrameContent {
                text(text)
            }
        ))
    }
}