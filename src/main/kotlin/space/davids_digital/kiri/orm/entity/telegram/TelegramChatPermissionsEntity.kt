package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "chat_permissions")
class TelegramChatPermissionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "can_send_messages", nullable = false)
    var canSendMessages: Boolean = false

    @Column(name = "can_send_audios", nullable = false)
    var canSendAudios: Boolean = false

    @Column(name = "can_send_documents", nullable = false)
    var canSendDocuments: Boolean = false

    @Column(name = "can_send_photos", nullable = false)
    var canSendPhotos: Boolean = false

    @Column(name = "can_send_videos", nullable = false)
    var canSendVideos: Boolean = false

    @Column(name = "can_send_video_notes", nullable = false)
    var canSendVideoNotes: Boolean = false

    @Column(name = "can_send_voice_notes", nullable = false)
    var canSendVoiceNotes: Boolean = false

    @Column(name = "can_send_polls", nullable = false)
    var canSendPolls: Boolean = false

    @Column(name = "can_send_other_messages", nullable = false)
    var canSendOtherMessages: Boolean = false

    @Column(name = "can_add_web_page_previews", nullable = false)
    var canAddWebPagePreviews: Boolean = false

    @Column(name = "can_change_info", nullable = false)
    var canChangeInfo: Boolean = false

    @Column(name = "can_invite_users", nullable = false)
    var canInviteUsers: Boolean = false

    @Column(name = "can_pin_messages", nullable = false)
    var canPinMessages: Boolean = false

    @Column(name = "can_manage_topics", nullable = false)
    var canManageTopics: Boolean = false
}
