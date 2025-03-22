package space.davids_digital.kiri.orm.service

import org.springframework.stereotype.Service
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.mapping.TelegramChatEntityMapper
import space.davids_digital.kiri.orm.mapping.TelegramMessageEntityMapper
import space.davids_digital.kiri.orm.repository.TelegramChatRepository
import space.davids_digital.kiri.orm.repository.TelegramMessageRepository

@Service
class TelegramOrmService(
    private val chatRepository: TelegramChatRepository,
    private val messageRepository: TelegramMessageRepository,
    private val chatMapper: TelegramChatEntityMapper,
    private val messageMapper: TelegramMessageEntityMapper
) {
    fun getAllChats(): List<TelegramChat> {
        return chatRepository.findAll().mapNotNull(chatMapper::toModel)
    }

    fun getChat(id: Long): TelegramChat? {
        return chatRepository.findById(id).map(chatMapper::toModel).orElse(null)
    }

    fun saveChat(chat: TelegramChat): TelegramChat {
        return chatMapper.toModel(chatRepository.save(chatMapper.toEntity(chat)!!))!!
    }

    fun deleteChat(id: Long) {
        chatRepository.deleteById(id)
    }

    fun saveMessage(message: TelegramMessage): TelegramMessage {
        return messageMapper.toModel(messageRepository.save(messageMapper.toEntity(message)!!))!!
    }

    fun getChatMessages(chatId: Long): List<TelegramMessage> {
        return messageRepository.getAllByIdChatId(chatId).mapNotNull(messageMapper::toModel)
    }
}