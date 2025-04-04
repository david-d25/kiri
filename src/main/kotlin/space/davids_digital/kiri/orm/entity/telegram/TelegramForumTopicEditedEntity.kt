package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(schema = "telegram", name = "forum_topics_edited")
class TelegramForumTopicEditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "name")
    var name: String? = null

    @Column(name = "icon_custom_emoji_id")
    var iconCustomEmojiId: String? = null
}