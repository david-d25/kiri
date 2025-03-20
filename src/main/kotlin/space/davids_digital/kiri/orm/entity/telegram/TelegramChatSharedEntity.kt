package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_chat_shared")
class TelegramChatSharedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "request_id", nullable = false)
    var requestId: Long = 0

    @Column(name = "chat_id", nullable = false)
    var chatId: Long = 0

    @Column(name = "title")
    var title: String? = null

    @Column(name = "username")
    var username: String? = null

    @ManyToMany
    @JoinTable(
        name = "telegram_chat_shared_photo_cross_links",
        joinColumns = [JoinColumn(name = "chat_shared_id", referencedColumnName = "internal_id")],
        inverseJoinColumns = [JoinColumn(name = "photo_id", referencedColumnName = "file_unique_id")]
    )
    var photo: MutableList<TelegramPhotoSizeEntity> = mutableListOf()
}
