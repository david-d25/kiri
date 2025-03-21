package space.davids_digital.kiri.integration.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramException
import com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.utility.kotlin.extension.request.getChat
import com.pengrad.telegrambot.utility.kotlin.extension.request.sendMessage
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.service.TelegramOrmService
import space.davids_digital.kiri.service.exception.ServiceException

@Service
class TelegramService(
    private val orm: TelegramOrmService,
    private val settings: Settings
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private lateinit var bot: TelegramBot

    @PostConstruct
    fun start() {
        bot = TelegramBot(settings.integration.telegram.apiKey)
        bot.setUpdatesListener(::processUpdates, ::onBotException)
    }

    suspend fun getChats(): List<TelegramChat> {
        return orm.getAllChats()
    }

    suspend fun getChat(username: String): TelegramChat {
        return bot.getChat(username)
            .checkNoErrors("Failed to get Telegram chat with username '$username'")
            .chat()
            .toModel()
            .let { orm.saveChat(it) }
    }

    suspend fun getChat(id: Long): TelegramChat {
        return bot.getChat(id)
            .checkNoErrors("Failed to get Telegram chat with id $id")
            .chat()
            .toModel()
            .let { orm.saveChat(it) }
    }

    suspend fun sendText(chatId: Long, text: String) {
        bot.sendMessage(chatId, text).checkNoErrors("Failed to send message to chat id $chatId")
    }

    suspend fun getChatMessages(chatId: Long): List<TelegramMessage> {
        return orm.getChatMessages(chatId)
    }

    private fun onMessageReceived(message: Message) {
        log.debug("Received Telegram message from chat {}", message.chat().id())
        orm.saveMessage(message.toModel())
        // TODO: Implement
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