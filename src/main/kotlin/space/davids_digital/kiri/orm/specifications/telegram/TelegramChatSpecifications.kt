package space.davids_digital.kiri.orm.specifications.telegram

import org.springframework.data.jpa.domain.Specification
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatEntity

object TelegramChatSpecifications {
    fun titleContains(text: String): Specification<TelegramChatEntity> =
        Specification { root, _, cb ->
            cb.like(cb.lower(root.get("title")), "%${text.lowercase()}%")
        }

    fun usernameContains(text: String): Specification<TelegramChatEntity> =
        Specification { root, _, cb ->
            cb.like(cb.lower(root.get("username")), "%${text.lowercase()}%")
        }

    fun typeIn(types: List<TelegramChatEntity.Type>): Specification<TelegramChatEntity> =
        Specification { root, _, cb ->
            root.get<TelegramChatEntity.Type>("type").`in`(types)
        }
}