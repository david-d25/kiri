package space.davids_digital.kiri.orm.specifications.telegram

import org.springframework.data.jpa.domain.Specification
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageEntity
import java.time.OffsetDateTime

object TelegramMessageSpecifications {
    fun chatId(chatId: Long): Specification<TelegramMessageEntity> =
        Specification { root, _, cb ->
            cb.equal(root.get<Any>("id").get<Long>("chatId"), chatId)
        }

    fun messageIdGreaterThan(messageId: Int): Specification<TelegramMessageEntity> =
        Specification { root, _, cb ->
            cb.greaterThan(root.get<Any>("id").get("messageId"), messageId)
        }

    fun messageIdLessThan(messageId: Int): Specification<TelegramMessageEntity> =
        Specification { root, _, cb ->
            cb.lessThan(root.get<Any>("id").get("messageId"), messageId)
        }

    fun dateAfter(date: OffsetDateTime): Specification<TelegramMessageEntity> =
        Specification { root, _, cb ->
            cb.greaterThanOrEqualTo(root.get("date"), date)
        }

    fun dateBefore(date: OffsetDateTime): Specification<TelegramMessageEntity> =
        Specification { root, _, cb ->
            cb.lessThanOrEqualTo(root.get("date"), date)
        }

    fun textContains(text: String): Specification<TelegramMessageEntity> =
        Specification { root, _, cb ->
            cb.like(cb.lower(root.get("text")), "%${text.lowercase()}%")
        }

    fun fromUser(userId: Long): Specification<TelegramMessageEntity> =
        Specification { root, _, cb ->
            cb.equal(root.get<Long>("fromId"), userId)
        }
}
