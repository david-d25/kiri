package space.davids_digital.kiri.integration.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.service.exception.ServiceException

@Service
class TelegramService(
    private val settings: Settings
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private lateinit var bot: Bot

    @PostConstruct
    fun start() {
        bot = bot {
            token = settings.integration.telegram.apiKey
            dispatch {
                message {
                    onMessageReceived(message)
                }
            }
        }
        bot.startPolling()
    }

    fun getChat(username: String): TelegramChat {
        return getChat(ChatId.ChannelUsername(username))
    }

    fun getChat(id: Long): TelegramChat {
        return getChat(ChatId.Id(id))
    }

    fun getDialogMessages(/* TODO */) /* TODO */ {
        // TODO: Implement
    }

    // TODO sending messages

    private fun getChat(id: ChatId): TelegramChat {
        return bot.getChat(id).fold(::entityToModel) { error ->
            throw ServiceException("Failed to get Telegram chat with id $id: $error")
        }
    }

    private fun onMessageReceived(message: Message) {
        log.debug("Received Telegram message: {}", message)
        message.chat.id
        // TODO: Implement
    }

    private fun entityToModel(entity: Chat): TelegramChat {
        return TODO()
//        return TelegramChat(
//            id = entity.id,
//            type = when (entity.type) {
//                "private" -> TelegramChat.Type.PRIVATE
//                "group" -> TelegramChat.Type.GROUP
//                "supergroup" -> TelegramChat.Type.SUPERGROUP
//                "channel" -> TelegramChat.Type.CHANNEL
//                else -> TelegramChat.Type.UNKNOWN
//            },
//            title = entity.title,
//            username = entity.username,
//            firstName = entity.firstName,
//            lastName = entity.lastName,
//            photo = entity.photo?.let {
//                TelegramChatPhoto(
//                    smallFileId = it.smallFileId,
//                    smallFileUniqueId = it.smallFileUniqueId,
//                    bigFileId = it.bigFileId,
//                    bigFileUniqueId = it.bigFileUniqueId,
//                )
//            },
//            bio = entity.bio,
//            description = entity.description,
//            inviteLink = entity.inviteLink,
//            pinnedMessage = entity.pinnedMessage?.let { entityToModel(it) }
//        )
    }

    private fun entityToModel(entity: Message): TelegramMessage {
        return TODO()
    }
}