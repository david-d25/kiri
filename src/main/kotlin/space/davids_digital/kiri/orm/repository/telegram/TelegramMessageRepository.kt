package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageEntity
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageEntityId

@Repository
interface TelegramMessageRepository: JpaRepository<TelegramMessageEntity, TelegramMessageEntityId>,
    JpaSpecificationExecutor<TelegramMessageEntity> {
    fun findByIdChatId(chatId: Long, pageable: Pageable): Page<TelegramMessageEntity>

    fun findFirstByIdChatId(chatId: Long, sort: Sort): TelegramMessageEntity?

    fun countByIdChatIdAndSeenFalse(chatId: Long): Long

    fun findByIdChatIdAndSeenFalse(chatId: Long, pageable: Pageable): Page<TelegramMessageEntity>

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

    @Modifying
    @Query(
        "update TelegramMessageEntity m set m.seen = true " +
                "where m.id.chatId = :chatId and m.id.messageId in :messageIds and m.seen = false"
    )
    fun markSeen(@Param("chatId") chatId: Long, @Param("messageIds") messageIds: Collection<Int>): Int
}