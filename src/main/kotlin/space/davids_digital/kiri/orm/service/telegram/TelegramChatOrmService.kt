package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.mapping.TelegramChatEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramChatRepository

@Service
class TelegramChatOrmService(
    private val repo: TelegramChatRepository,
    private val mapper: TelegramChatEntityMapper,
) {
    @Transactional(readOnly = true)
    fun getAllChats(): List<TelegramChat> {
        return repo.findAll().mapNotNull(mapper::toModel)
    }

    @Transactional(readOnly = true)
    fun getChat(id: Long): TelegramChat? {
        return repo.findById(id).map(mapper::toModel).orElse(null)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return repo.existsById(id)
    }

    @Transactional
    fun saveChat(chat: TelegramChat): TelegramChat {
        return mapper.toModel(repo.save(mapper.toEntity(chat)!!))!!
    }

    @Transactional
    fun deleteChat(id: Long) {
        repo.deleteById(id)
    }
}