package space.davids_digital.kiri.orm.service

import org.springframework.stereotype.Service
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.mapping.toEntity
import space.davids_digital.kiri.orm.mapping.toModel
import space.davids_digital.kiri.orm.repository.TelegramChatRepository
import space.davids_digital.kiri.orm.repository.TelegramMessageRepository

@Service
class TelegramOrmService(
    private val chatRepository: TelegramChatRepository,
    private val messageRepository: TelegramMessageRepository,
) {
    fun getAllChats(): List<TelegramChat> {
        return chatRepository.findAll().map { it.toModel() }
    }

    fun getChat(id: Long): TelegramChat? {
        return chatRepository.findById(id).map { it.toModel() }.orElse(null)
    }

    fun saveChat(chat: TelegramChat): TelegramChat {
        return chatRepository.save(chat.toEntity()).toModel()
    }

    fun deleteChat(id: Long) {
        chatRepository.deleteById(id)
    }

    fun saveMessage(message: TelegramMessage): TelegramMessage {
        return messageRepository.save(message.toEntity()).toModel()
    }

    fun getChatMessages(chatId: Long): List<TelegramMessage> {
        return messageRepository.getAllByIdChatId(chatId).map { it.toModel() }
    }
}