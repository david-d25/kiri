package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageId
import java.time.OffsetDateTime

@Entity
@Table(name = "telegram_messages")
class TelegramMessageEntity {
    @EmbeddedId
    var id: TelegramMessageId = TelegramMessageId()

    @Column(name = "message_thread_id")
    var messageThreadId: Long? = null

    @Column(name = "date")
    var date: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "edit_date")
    var editDate: OffsetDateTime? = null

    @Column(name = "from_id")
    var fromId: Long? = null

    @Column(name = "sender_chat_id")
    var senderChatId: Long? = null

    @Column(name = "sender_boost_count")
    var senderBoostCount: Long? = null

    @Column(name = "sender_business_bot_id")
    var senderBusinessBotId: Long? = null

    @Column(name = "business_connection_id")
    var businessConnectionId: String? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "forward_origin_id", referencedColumnName = "internal_id")
    var forwardOrigin: TelegramMessageOriginEntity? = null

    @Column(name = "is_topic_message")
    var isTopicMessage: Boolean = false

    @Column(name = "is_automatic_forward")
    var isAutomaticForward: Boolean = false

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "reply_to_message__chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "reply_to_message__message_id", referencedColumnName = "message_id")
    )
    var replyToMessage: TelegramMessageEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "external_reply_info_id", referencedColumnName = "internal_id")
    var externalReplyInfo: TelegramExternalReplyInfoEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "quote_id", referencedColumnName = "internal_id")
    var quote: TelegramTextQuoteEntity? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "reply_to_story__chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "reply_to_story__story_id", referencedColumnName = "story_id")
    )
    var replyToStory: TelegramStoryEntity? = null

    @ManyToOne
    @JoinColumn(name = "via_bot_id", referencedColumnName = "id")
    var viaBot: TelegramUserEntity? = null

    @Column(name = "has_protected_content")
    var hasProtectedContent: Boolean = false

    @Column(name = "is_from_offline")
    var isFromOffline: Boolean = false

    @Column(name = "media_group_id")
    var mediaGroupId: String? = null

    @Column(name = "author_signature")
    var authorSignature: String? = null

    @Column(name = "text")
    var text: String? = null

    @OneToMany(
        mappedBy = "parentMessageText",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        targetEntity = TelegramMessageEntityEntity::class
    )
    var entities: List<TelegramMessageEntityEntity> = mutableListOf()

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "link_preview_options_id", referencedColumnName = "internal_id")
    var linkPreviewOptions: TelegramLinkPreviewOptionsEntity? = null

    @Column(name = "effect_id")
    var effectId: String? = null

    @ManyToOne
    @JoinColumn(name = "animation_id", referencedColumnName = "file_unique_id")
    var animation: TelegramAnimationEntity? = null

    @ManyToOne
    @JoinColumn(name = "audio_id", referencedColumnName = "file_unique_id")
    var audio: TelegramAudioEntity? = null

    @ManyToOne
    @JoinColumn(name = "document_id", referencedColumnName = "file_unique_id")
    var document: TelegramDocumentEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "paid_media_info_id", referencedColumnName = "internal_id")
    var paidMediaInfo: TelegramPaidMediaInfoEntity? = null

    @ManyToMany
    @JoinTable(
        name = "telegram_messages_photo_cross_links",
        joinColumns = [
            JoinColumn(name = "chat_id", referencedColumnName = "chat_id"),
            JoinColumn(name = "message_id", referencedColumnName = "message_id")
        ],
        inverseJoinColumns = [
            JoinColumn(name = "photo_id", referencedColumnName = "file_unique_id")
        ]
    )
    var photo: MutableList<TelegramPhotoSizeEntity> = mutableListOf()

    @ManyToOne
    @JoinColumn(name = "sticker_id", referencedColumnName = "file_unique_id")
    var sticker: TelegramStickerEntity? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "story_chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "story_id", referencedColumnName = "story_id")
    )
    var story: TelegramStoryEntity? = null

    @ManyToOne
    @JoinColumn(name = "video_id", referencedColumnName = "file_unique_id")
    var video: TelegramVideoEntity? = null

    @ManyToOne
    @JoinColumn(name = "video_note_id", referencedColumnName = "file_unique_id")
    var videoNote: TelegramVideoNoteEntity? = null

    @ManyToOne
    @JoinColumn(name = "voice_id", referencedColumnName = "file_unique_id")
    var voice: TelegramVoiceEntity? = null

    @Column(name = "caption")
    var caption: String? = null

    @OneToMany(
        mappedBy = "parentMessageCaption",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        targetEntity = TelegramMessageEntityEntity::class
    )
    var captionEntities: List<TelegramMessageEntityEntity> = mutableListOf()

    @Column(name = "show_caption_above_media")
    var showCaptionAboveMedia: Boolean = false

    @Column(name = "has_media_spoiler")
    var hasMediaSpoiler: Boolean = false

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "contact_id", referencedColumnName = "internal_id")
    var contact: TelegramContactEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "dice_id", referencedColumnName = "internal_id")
    var dice: TelegramDiceEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "game_id", referencedColumnName = "internal_id")
    var game: TelegramGameEntity? = null

    @ManyToOne
    @JoinColumn(name = "poll_id", referencedColumnName = "id")
    var poll: TelegramPollEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "venue_id", referencedColumnName = "internal_id")
    var venue: TelegramVenueEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "location_id", referencedColumnName = "internal_id")
    var location: TelegramLocationEntity? = null

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "new_chat_members")
    var newChatMembers: Array<Long>? = null

    @Column(name = "left_chat_member_id")
    var leftChatMemberId: Long? = null

    @Column(name = "new_chat_title")
    var newChatTitle: String? = null

    @ManyToMany
    @JoinTable(
        name = "telegram_messages_new_chat_photo_cross_links",
        joinColumns = [
            JoinColumn(name = "chat_id", referencedColumnName = "chat_id"),
            JoinColumn(name = "message_id", referencedColumnName = "message_id")
        ],
        inverseJoinColumns = [
            JoinColumn(name = "photo_id", referencedColumnName = "file_unique_id")
        ]
    )
    var newChatPhoto: MutableList<TelegramPhotoSizeEntity> = mutableListOf()

    @Column(name = "group_chat_created")
    var groupChatCreated: Boolean = false

    @Column(name = "supergroup_chat_created")
    var supergroupChatCreated: Boolean = false

    @Column(name = "channel_chat_created")
    var channelChatCreated: Boolean = false

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "message_auto_delete_timer_changed_id", referencedColumnName = "internal_id")
    var messageAutoDeleteTimerChanged: TelegramMessageAutoDeleteTimerChangedEntity? = null

    @Column(name = "migrate_to_chat_id")
    var migrateToChatId: Long? = null

    @Column(name = "migrate_from_chat_id")
    var migrateFromChatId: Long? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "pinned_message_chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "pinned_message_message_id", referencedColumnName = "message_id")
    )
    var pinnedMessage: TelegramMessageEntity? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "pinned_message_chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
        JoinColumn(name = "pinned_message_message_id", referencedColumnName = "message_id", insertable = false, updatable = false)
    )
    var pinnedInaccessibleMessage: TelegramInaccessibleMessageEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "invoice_id", referencedColumnName = "internal_id")
    var invoice: TelegramInvoiceEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "successful_payment_id", referencedColumnName = "internal_id")
    var successfulPayment: TelegramSuccessfulPaymentEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "refunded_payment_id", referencedColumnName = "internal_id")
    var refundedPayment: TelegramRefundedPaymentEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "users_shared_id", referencedColumnName = "internal_id")
    var usersShared: TelegramUsersSharedEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "chat_shared_id", referencedColumnName = "internal_id")
    var chatShared: TelegramChatSharedEntity? = null

    @Column(name = "connected_website")
    var connectedWebsite: String? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "write_access_allowed_id", referencedColumnName = "internal_id")
    var writeAccessAllowed: TelegramWriteAccessAllowedEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "passport_data_id", referencedColumnName = "internal_id")
    var passportData: TelegramPassportDataEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "proximity_alert_triggered_id", referencedColumnName = "internal_id")
    var proximityAlertTriggered: TelegramProximityAlertTriggeredEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "chat_boost_added_id", referencedColumnName = "internal_id")
    var chatBoostAdded: TelegramChatBoostAddedEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "chat_background_set_id", referencedColumnName = "internal_id")
    var chatBackgroundSet: TelegramChatBackgroundEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "forum_topic_created_id", referencedColumnName = "internal_id")
    var forumTopicCreated: TelegramForumTopicCreatedEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "forum_topic_edited_id", referencedColumnName = "internal_id")
    var forumTopicEdited: TelegramForumTopicEditedEntity? = null

    @Column(name = "forum_topic_closed")
    var forumTopicClosed: Boolean = false

    @Column(name = "forum_topic_reopened")
    var forumTopicReopened: Boolean = false

    @Column(name = "general_forum_topic_hidden")
    var generalForumTopicHidden: Boolean = false

    @Column(name = "general_forum_topic_unhidden")
    var generalForumTopicUnhidden: Boolean = false

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "giveaway_created_id", referencedColumnName = "internal_id")
    var giveawayCreated: TelegramGiveawayCreatedEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "giveaway_id", referencedColumnName = "internal_id")
    var giveaway: TelegramGiveawayEntity? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "giveaway_winners_chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "giveaway_winners_message_id", referencedColumnName = "giveaway_message_id")
    )
    var giveawayWinners: TelegramGiveawayWinnersEntity? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "giveaway_completed_chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "giveaway_completed_message_id", referencedColumnName = "message_id")
    )
    var giveawayCompleted: TelegramGiveawayCompletedEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "video_chat_scheduled_id", referencedColumnName = "internal_id")
    var videoChatScheduled: TelegramVideoChatScheduledEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "video_chat_ended_id", referencedColumnName = "internal_id")
    var videoChatEnded: TelegramVideoChatEndedEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "video_chat_participants_invited_id", referencedColumnName = "internal_id")
    var videoChatParticipantsInvited: TelegramVideoChatParticipantsInvitedEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "web_app_data_id", referencedColumnName = "internal_id")
    var webAppData: TelegramWebAppDataEntity? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "reply_markup_id", referencedColumnName = "internal_id")
    var replyMarkup: TelegramInlineKeyboardMarkupEntity? = null
}
