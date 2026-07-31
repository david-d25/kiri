package space.davids_digital.kiri.orm.service.telegram

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramMessageReaction
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageReactionsEntity
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageEntityId
import space.davids_digital.kiri.orm.repository.telegram.TelegramMessageReactionsRepository
import java.time.OffsetDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class TelegramMessageReactionsOrmService(
    private val repo: TelegramMessageReactionsRepository,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun get(chatId: Long, messageId: Int): List<TelegramMessageReaction> {
        val entity = repo.findById(TelegramMessageEntityId(chatId, messageId)).getOrNull() ?: return emptyList()
        return deserialize(entity.reactions)
    }

    /**
     * Replaces the stored reactions of a message with the given list. Reactions with a non-positive count are
     * dropped, and an empty result is stored as-is so a fully cleared message renders no reactions.
     */
    @Transactional
    fun put(chatId: Long, messageId: Int, reactions: List<TelegramMessageReaction>) {
        val id = TelegramMessageEntityId(chatId, messageId)
        val entity = repo.findById(id).getOrNull() ?: TelegramMessageReactionsEntity().apply { this.id = id }
        entity.reactions = objectMapper.writeValueAsString(reactions.filter { it.count > 0 })
        entity.updatedAt = OffsetDateTime.now()
        repo.save(entity)
    }

    private fun deserialize(json: String): List<TelegramMessageReaction> {
        return try {
            objectMapper.readValue(json)
        } catch (e: Exception) {
            log.warn("Failed to deserialize stored reactions: {}", json, e)
            emptyList()
        }
    }
}
