package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.orm.entity.telegram.TelegramPollAnswerEntity
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramPollAnswerEntityId
import space.davids_digital.kiri.orm.repository.telegram.TelegramPollAnswerRepository
import java.time.OffsetDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class TelegramPollAnswerOrmService(
    private val repo: TelegramPollAnswerRepository,
) {
    @Transactional(readOnly = true)
    fun get(pollId: String, voterId: Long): List<Int> {
        return repo.findById(TelegramPollAnswerEntityId(pollId, voterId)).getOrNull()?.optionIds?.toList()
            ?: emptyList()
    }

    /**
     * Stores a voter's current selection. An empty selection (retracted vote) removes the row, since there is
     * nothing to remember.
     */
    @Transactional
    fun put(pollId: String, voterId: Long, optionIds: List<Int>) {
        val id = TelegramPollAnswerEntityId(pollId, voterId)
        if (optionIds.isEmpty()) {
            repo.deleteById(id)
            return
        }
        val entity = repo.findById(id).getOrNull() ?: TelegramPollAnswerEntity().apply { this.id = id }
        entity.optionIds = optionIds.toTypedArray()
        entity.updatedAt = OffsetDateTime.now()
        repo.save(entity)
    }
}
