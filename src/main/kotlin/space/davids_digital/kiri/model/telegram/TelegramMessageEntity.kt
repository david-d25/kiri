package space.davids_digital.kiri.model.telegram

/**
 * Represents one special entity in a text message. For example, hashtags, usernames, URLs, etc.
 */
data class TelegramMessageEntity(
    /**
     * Type of the entity.
     */
    val type: Type,
    /**
     * Offset in [UTF-16 code units](https://core.telegram.org/api/entities#entity-length) to the start of the entity.
     */
    val offset: Int,
    /**
     * Length of the entity in [UTF-16 code units](https://core.telegram.org/api/entities#entity-length).
     */
    val length: Int,
    /**
     * For [Type.TEXT_LINK] only, URL that will be opened after user taps on the text.
     */
    val url: String? = null,
    /**
     * For [Type.TEXT_MENTION] only, the mentioned user.
     */
    val userId: Long? = null,
    /**
     * For [Type.PRE] only, the programming language of the entity text.
     */
    val language: String? = null,
    /**
     * For [Type.CUSTOM_EMOJI] only, unique identifier of the custom emoji.
     * Use [getCustomEmojiStickers](https://core.telegram.org/bots/api#getcustomemojistickers) to get full information
     * about the sticker.
     */
    val customEmojiId: String? = null,
) {
    enum class Type {
        MENTION,        // @username
        HASHTAG,        // #hashtag or #hashtag@chatusername
        CASHTAG,        // $USD or $USD@chatusername
        BOT_COMMAND,    // /start@jobs_bot
        URL,            // https://telegram.org
        EMAIL,          // do-not-reply@telegram.org
        PHONE_NUMBER,   // +1-212-555-0123
        BOLD,
        ITALIC,
        UNDERLINE,
        STRIKETHROUGH,
        SPOILER,
        BLOCKQUOTE,
        EXPANDABLE_BLOCKQUOTE,
        CODE,
        PRE,
        TEXT_LINK,
        TEXT_MENTION,
        CUSTOM_EMOJI
    }
}