package space.davids_digital.kiri.integration.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramException
import com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.utility.kotlin.extension.request.getChat
import com.pengrad.telegrambot.utility.kotlin.extension.request.sendMessage
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.agent.engine.EngineEvent
import space.davids_digital.kiri.agent.engine.EngineEventBus
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.notification.Notification
import space.davids_digital.kiri.agent.notification.NotificationManager
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramMessageOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramUserOrmService
import space.davids_digital.kiri.service.exception.ServiceException
import java.time.ZonedDateTime

@Service
class TelegramService(
    private val messageOrm: TelegramMessageOrmService,
    private val chatOrm: TelegramChatOrmService,
    private val userOrm: TelegramUserOrmService,
    private val settings: Settings,
    private val engineEventBus: EngineEventBus,
    private val notificationManager: NotificationManager,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private lateinit var bot: TelegramBot

    @PostConstruct
    fun start() {
        bot = TelegramBot(settings.integration.telegram.apiKey)
        bot.setUpdatesListener(::processUpdates, ::onBotException)
    }

    suspend fun getChats(): List<TelegramChat> {
        return chatOrm.getAllChats()
    }

    suspend fun getChat(username: String): TelegramChat {
        return bot.getChat(username)
            .checkNoErrors("Failed to get Telegram chat with username '$username'")
            .chat()
            .toModel()
            .let { chatOrm.saveChat(it) }
    }

    suspend fun getChat(id: Long): TelegramChat {
        return bot.getChat(id)
            .checkNoErrors("Failed to get Telegram chat with id $id")
            .chat()
            .toModel()
            .let { chatOrm.saveChat(it) }
    }

    fun getUser(id: Long) = userOrm.findById(id)

    suspend fun sendText(chatId: Long, text: String) {
        val message = bot.sendMessage(chatId, text).checkNoErrors("Failed to send message to chat id $chatId").message()
        try {
            messageOrm.save(message.toModel())
        } catch (e: Exception) {
            log.error("Failed to save Telegram message", e)
            notificationManager.push(Notification(
                sentAt = ZonedDateTime.now(),
                metadata = mapOf("app" to "telegram"),
                content = dataFrameContent {
                    text("Message is sent, but failed to save it into local database.")
                }
            ))
        }
    }

    suspend fun getChatMessages(chatId: Long): List<TelegramMessage> {
        return messageOrm.getChatMessages(chatId)
    }

    private fun onMessageReceived(message: Message) {
        log.debug("Received Telegram message from chat {}", message.chat().id())
        message.chat()?.let(::refreshChat)
        message.from()?.let(::refreshUser)
        try {
            messageOrm.save(message.toModel())
        } catch (e: Exception) {
            log.error("Failed to save Telegram message", e)
        }
        sendNewMessageNotification(message)
    }

    private fun sendNewMessageNotification(message: Message) {
        val user = message.from()?.toModel()
        notificationManager.push(Notification(
            sentAt = ZonedDateTime.now(),
            metadata = mapOf("app" to "telegram"),
            content = dataFrameContent {
                val from = user?.firstName ?: "(user)"
                val text = message.text()
                text("$from: $text")
            }
        ))
    }

    private fun refreshUser(user: User) {
        userOrm.save(user.toModel())
    }

    private fun refreshChat(chat: Chat) {
        val id = chat.id()
        try {
            if (!chatOrm.existsById(id)) {
                log.debug("Chat with id $id does not exist, fetching and saving")
                val chat = bot.getChat(id)
                    .checkNoErrors("Failed to get Telegram chat with id $id")
                    .chat()
                    .toModel()
                chatOrm.saveChat(chat)
            }
        } catch (e: Exception) {
            log.error("Failed to get and save Telegram chat", e)
        }
    }

    private fun processUpdates(updates: List<Update>): Int {
        updates.forEach { update ->
            if (update.message() != null) {
                onMessageReceived(update.message())
            } else {
                log.warn("Unsupported update: {}", update) // TODO other updates
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
}