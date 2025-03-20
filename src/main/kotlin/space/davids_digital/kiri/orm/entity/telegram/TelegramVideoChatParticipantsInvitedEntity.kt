package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_video_chat_participants_invited")
class TelegramVideoChatParticipantsInvitedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @ElementCollection
    @CollectionTable(
        name = "telegram_video_chat_invited_user_ids",
        joinColumns = [JoinColumn(name = "invited_id", referencedColumnName = "internal_id")]
    )
    @Column(name = "user_id", nullable = false)
    var users: MutableList<Long> = mutableListOf()
}
