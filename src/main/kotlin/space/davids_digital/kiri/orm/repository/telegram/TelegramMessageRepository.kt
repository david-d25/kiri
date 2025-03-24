package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageEntity
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageId

@Repository
interface TelegramMessageRepository: JpaRepository<TelegramMessageEntity, TelegramMessageId> {
    fun getAllByIdChatId(chatId: Long): List<TelegramMessageEntity>
}