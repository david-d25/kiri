package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageEntity
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageEntityId

@Repository
interface TelegramMessageRepository: JpaRepository<TelegramMessageEntity, TelegramMessageEntityId> {
    fun getAllByIdChatId(chatId: Long, pageable: Pageable): List<TelegramMessageEntity>
}