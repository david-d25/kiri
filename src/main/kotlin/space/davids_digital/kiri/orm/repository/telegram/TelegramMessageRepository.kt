package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageEntity
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageEntityId

@Repository
interface TelegramMessageRepository: JpaRepository<TelegramMessageEntity, TelegramMessageEntityId> {
    fun findByIdChatId(chatId: Long, pageable: Pageable): Page<TelegramMessageEntity>

    fun findFirstByIdChatId(chatId: Long, sort: Sort): TelegramMessageEntity?

    fun countByIdChatIdAndIdMessageIdGreaterThan(chatId: Long, afterMessageId: Int): Long

    fun findByIdChatIdAndIdMessageIdGreaterThanEqual(
        chatId: Long,
        sinceMessageId: Int,
        pageable: Pageable
    ): Page<TelegramMessageEntity>

    fun findByIdChatIdAndIdMessageIdLessThan(
        chatId: Long,
        beforeMessageId: Int,
        pageable: Pageable
    ): Page<TelegramMessageEntity>
}