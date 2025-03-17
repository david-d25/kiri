package space.davids_digital.kiri.integration.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramException
import com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.utility.kotlin.extension.request.getChat
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.repository.TelegramChatRepository
import space.davids_digital.kiri.service.exception.ServiceException

@Service
class TelegramService(
    private val telegramChatRepository: TelegramChatRepository,
    private val settings: Settings
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private lateinit var bot: TelegramBot

    @PostConstruct
    fun start() {
        bot = TelegramBot(settings.integration.telegram.apiKey)
        bot.setUpdatesListener(::processUpdates, ::onBotException)
    }

    fun getChats(): List<TelegramChat> {
        val ids = telegramChatRepository.findAll().map { it.id }

        TODO()
    }

    fun getChat(username: String): TelegramChat {
        return bot.getChat(username)
            .checkNoErrors("Failed to get Telegram chat with username '$username'")
            .chat()
            .toModel()
    }

    fun getChat(id: Long): TelegramChat {
        return bot.getChat(id)
            .checkNoErrors("Failed to get Telegram chat with id $id")
            .chat()
            .toModel()
    }

    fun getDialogMessages(/* TODO */) /* TODO */ {
        // TODO: Implement
    }

    // TODO sending messages

    private fun onMessageReceived(message: Message) {
        log.debug("Received Telegram message: {}", message)
        // TODO: Implement
    }

    private fun processUpdates(updates: List<Update>): Int {
        // TODO
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