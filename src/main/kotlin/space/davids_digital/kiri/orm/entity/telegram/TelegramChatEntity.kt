package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "chats")
class TelegramChatEntity {
    @Id
    @Column(name = "id")
    var id: Long = 0

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    var type: Type = Type.PRIVATE

    @Column(name = "title")
    var title: String? = null

    @Column(name = "username")
    var username: String? = null

    @Column(name = "first_name")
    var firstName: String? = null

    @Column(name = "last_name")
    var lastName: String? = null

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    @JoinColumn(name = "photo_id", referencedColumnName = "internal_id")
    var photo: TelegramChatPhotoEntity? = null

    @Column(name = "bio")
    var bio: String? = null

    @Column(name = "description")
    var description: String? = null

    @Column(name = "invite_link")
    var inviteLink: String? = null

    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinColumns(
        JoinColumn(name = "pinned_message_chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "pinned_message_message_id", referencedColumnName = "message_id")
    )
    var pinnedMessage: TelegramMessageEntity? = null

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    @JoinColumn(name = "permissions_id", referencedColumnName = "internal_id")
    var permissions: TelegramChatPermissionsEntity? = null

    @Column(name = "slow_mode_delay")
    var slowModeDelay: Int? = null

    @Column(name = "sticker_set_name")
    var stickerSetName: String? = null

    @Column(name = "can_set_sticker_set")
    var canSetStickerSet: Boolean? = null

    @Column(name = "linked_chat_id")
    var linkedChatId: Long? = null

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    @JoinColumn(name = "location_id", referencedColumnName = "internal_id")
    var location: TelegramChatLocationEntity? = null

    enum class Type {
        PRIVATE, GROUP, SUPERGROUP, CHANNEL
    }
}
