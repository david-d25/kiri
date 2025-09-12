package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatEntity

@Repository
interface TelegramChatRepository:
    JpaRepository<TelegramChatEntity, Long>,
    JpaSpecificationExecutor<TelegramChatEntity>
{
    fun findByUsername(username: String): TelegramChatEntity?
    @Query("""
        select chat from TelegramChatEntity chat
            left join TelegramChatMetadataEntity metadata on chat.id = metadata.chatId
            where metadata.enabled
    """)
    fun findAllEnabled(pageable: Pageable): Page<TelegramChatEntity>
}