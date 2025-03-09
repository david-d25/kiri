package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "telegram_chats")
class TelegramChatEntity {
    @Id
    var id: Long = 0
}