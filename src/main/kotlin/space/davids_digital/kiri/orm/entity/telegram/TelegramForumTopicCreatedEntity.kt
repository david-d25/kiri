package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "forum_topics_created")
class TelegramForumTopicCreatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "name", nullable = false)
    var name: String = ""

    @Column(name = "icon_color", nullable = false)
    var iconColor: Int = 0  // Color as RGB integer

    @Column(name = "icon_custom_emoji_id")
    var iconCustomEmojiId: String? = null
}
