package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramStoryId

@Entity
@Table(schema = "telegram", name = "stories")
@IdClass(TelegramStoryId::class)
class TelegramStoryEntity {
    @Id
    @Column(name = "chat_id")
    var chatId: Long = 0

    @Id
    @Column(name = "story_id")
    var storyId: Long = 0
}
