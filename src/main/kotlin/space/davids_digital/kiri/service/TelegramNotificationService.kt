package space.davids_digital.kiri.service

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.engine.EngineEventBus
import space.davids_digital.kiri.agent.engine.WakeUpRequestEvent
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.notification.Notification
import space.davids_digital.kiri.agent.notification.NotificationManager
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.model.telegram.TelegramUpdate
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentSkipListSet
import javax.annotation.PostConstruct

@Service
class TelegramNotificationService (
    private val telegram: TelegramService,
    private val notificationManager: NotificationManager,
    private val engineEventBus: EngineEventBus
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val chatsOpenedInAgentApp = ConcurrentSkipListSet<Long>()

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
                    handleUpdate(update)
                } catch (e: Exception) {
                    log.error("Error handling update", e)
                }
            }
        }
    }

    private suspend fun handleUpdate(update: TelegramUpdate) {
        when {
            update.message != null -> handleMessage(update.message)
        }
    }

    private suspend fun handleMessage(message: TelegramMessage) {
        val chat = telegram.getChat(message.chatId)
        if (chat == null) {
            log.warn("Received message from unknown chat with id {}", message.chatId)
            return
        }
        if (!chat.metadata.enabled) {
            log.debug("Chat ${chat.id} is disabled, skipping")
            return // Chat is disabled
        }
        val self = telegram.getSelf()
        val isAgentMentioned = message.text?.contains("@" + self.username) == true
        val isPrivateChat = chat.type == TelegramChat.Type.PRIVATE
        val isAgentMessageRepliedTo = message.replyToMessage?.fromId == self.id
        val chatIsOpenedInApp = message.chatId in chatsOpenedInAgentApp
        if (chatIsOpenedInApp) {
            // Current chat is opened in the agent app, just wake up the agent
            if (isPrivateChat || isAgentMentioned || isAgentMessageRepliedTo) {
                sentNotification("New message in current chat")
                return
            }
        }
        if (isPrivateChat) {
            val userDisplayName = when {
                chat.username != null && chat.firstName != null -> chat.firstName + " (@${chat.username})"
                chat.username != null -> "@" + chat.username
                chat.firstName != null -> chat.firstName + " (${chat.id})"
                else -> "(unknown user id ${chat.id})"
            }
            sentNotification("New message from $userDisplayName")
            return
        }
        if (isAgentMentioned) {
            val from = when {
                chat.username != null -> "@" + chat.username
                chat.title != null -> "'${chat.title}'"
                else -> "(unknown chat id ${chat.id})"
            }
            sentNotification("You were mentioned in chat $from")
            return
        }
        if (isAgentMessageRepliedTo) {
            val user = message.fromId?.let { telegram.getUser(it) }
            val chatDisplayName = when {
                chat.username != null -> "@" + chat.username
                chat.title != null -> "'${chat.title}'"
                else -> "(unknown chat id ${chat.id})"
            }
            val userDisplayName = when {
                user == null -> "(unknown user)"
                user.username != null -> "@" + user.username
                else -> user.firstName + " (${user.id})"
            }
            sentNotification("$userDisplayName replied to you in chat $chatDisplayName")
            return
        }
    }

    private fun tryWakeUpAgent() {
        val emitResult = engineEventBus.events.tryEmit(WakeUpRequestEvent())
        if (!emitResult) {
            log.error("Could not emit wake up event")
        }
    }

    private suspend fun sentNotification(text: String) {
        notificationManager.push(Notification(
            sentAt = ZonedDateTime.now(),
            metadata = mapOf(
                "app" to "telegram"
            ),
            content = dataFrameContent {
                text(text)
            }
        ))
    }
}