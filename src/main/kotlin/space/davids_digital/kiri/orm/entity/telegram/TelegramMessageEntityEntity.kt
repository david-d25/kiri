package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_message_entities")
class TelegramMessageEntityEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @ManyToOne
    @JoinColumn(name = "parent_game_text_id")
    var parentGame: TelegramGameEntity? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "parent_message_text_chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "parent_message_text_message_id", referencedColumnName = "message_id")
    )
    var parentMessageText: TelegramMessageEntity? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "parent_message_caption_chat_id", referencedColumnName = "chat_id"),
        JoinColumn(name = "parent_message_caption_message_id", referencedColumnName = "message_id")
    )
    var parentMessageCaption: TelegramMessageEntity? = null

    @ManyToOne
    @JoinColumn(name = "parent_poll_question_id")
    var parentPollQuestion: TelegramPollEntity? = null

    @ManyToOne
    @JoinColumn(name = "parent_poll_explanation_id")
    var parentPollExplanation: TelegramPollEntity? = null

    @ManyToOne
    @JoinColumn(name = "parent_poll_option_text_id")
    var parentPollOption: TelegramPollOptionEntity? = null

    @ManyToOne
    @JoinColumn(name = "parent_text_quote_id")
    var parentTextQuote: TelegramTextQuoteEntity? = null

    @Column(name = "type")
    var type: String = "" // MENTION, HASHTAG, etc.

    @Column(name = "offset")
    var offset: Int = 0

    @Column(name = "length")
    var length: Int = 0

    @Column(name = "url")
    var url: String? = null

    @Column(name = "user_id")
    var userId: Long? = null

    @Column(name = "language")
    var language: String? = null

    @Column(name = "custom_emoji_id")
    var customEmojiId: String? = null
}
