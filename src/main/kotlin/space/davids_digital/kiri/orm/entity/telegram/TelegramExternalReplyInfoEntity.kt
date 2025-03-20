package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_external_reply_info")
class TelegramExternalReplyInfoEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @OneToOne
    @JoinColumn(name = "origin_id", referencedColumnName = "internal_id")
    var origin: TelegramMessageOriginEntity? = null

    @Column(name = "chat_id")
    var chatId: Long? = null

    @Column(name = "message_id")
    var messageId: Long? = null

    @OneToOne
    @JoinColumn(name = "link_preview_options_id", referencedColumnName = "internal_id")
    var linkPreviewOptions: TelegramLinkPreviewOptionsEntity? = null

    @OneToOne @JoinColumn(name = "animation_id", referencedColumnName = "file_unique_id")
    var animation: TelegramAnimationEntity? = null

    @OneToOne @JoinColumn(name = "audio_id", referencedColumnName = "file_unique_id")
    var audio: TelegramAudioEntity? = null

    @OneToOne @JoinColumn(name = "document_id", referencedColumnName = "file_unique_id")
    var document: TelegramDocumentEntity? = null

    @OneToOne @JoinColumn(name = "paid_media_info_id", referencedColumnName = "internal_id")
    var paidMedia: TelegramPaidMediaInfoEntity? = null

    @OneToMany
    @JoinTable(
        name = "telegram_external_reply_info_photo_cross_links",
        joinColumns = [JoinColumn(name = "reply_info_id", referencedColumnName = "internal_id")],
        inverseJoinColumns = [JoinColumn(name = "photo_id", referencedColumnName = "file_unique_id")]
    )
    var photo: MutableList<TelegramPhotoSizeEntity> = mutableListOf()

    @OneToOne @JoinColumn(name = "sticker_id", referencedColumnName = "file_unique_id")
    var sticker: TelegramStickerEntity? = null

    @OneToOne @JoinColumn(name = "story_chat_id", referencedColumnName = "chat_id")
    var story: TelegramStoryEntity? = null

    @OneToOne @JoinColumn(name = "video_id", referencedColumnName = "file_unique_id")
    var video: TelegramVideoEntity? = null

    @OneToOne @JoinColumn(name = "video_note_id", referencedColumnName = "file_unique_id")
    var videoNote: TelegramVideoNoteEntity? = null

    @Column(name = "has_media_spoiler")
    var hasMediaSpoiler: Boolean = false

    @OneToOne @JoinColumn(name = "contact_id", referencedColumnName = "internal_id")
    var contact: TelegramContactEntity? = null

    @OneToOne @JoinColumn(name = "dice_id", referencedColumnName = "internal_id")
    var dice: TelegramDiceEntity? = null

    @OneToOne @JoinColumn(name = "game_id", referencedColumnName = "internal_id")
    var game: TelegramGameEntity? = null

    @OneToOne @JoinColumn(name = "giveaway_id", referencedColumnName = "internal_id")
    var giveaway: TelegramGiveawayEntity? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "giveaway_winners_chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "giveaway_winners_message_id", referencedColumnName = "message_id")
    )
    var giveawayWinners: TelegramGiveawayWinnersEntity? = null

    @OneToOne @JoinColumn(name = "invoice_id", referencedColumnName = "internal_id")
    var invoice: TelegramInvoiceEntity? = null

    @OneToOne @JoinColumn(name = "location_id", referencedColumnName = "internal_id")
    var location: TelegramLocationEntity? = null

    @OneToOne @JoinColumn(name = "poll_id", referencedColumnName = "id")
    var poll: TelegramPollEntity? = null

    @OneToOne @JoinColumn(name = "venue_id", referencedColumnName = "internal_id")
    var venue: TelegramVenueEntity? = null
}
