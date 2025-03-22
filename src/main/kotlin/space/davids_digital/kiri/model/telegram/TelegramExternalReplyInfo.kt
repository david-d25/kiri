package space.davids_digital.kiri.model.telegram

/**
 * Represents information about a message that is being replied to, which may come from another chat or forum topic.
 */
data class TelegramExternalReplyInfo (
    /**
     * Origin of the message replied to by the given message.
     */
    val origin: TelegramMessageOrigin,
    /**
     * Chat the original message belongs to. Available only if the chat is a supergroup or a channel.
     */
    val chatId: Long? = null,
    /**
     * Unique message identifier inside the original chat.
     * Available only if the original chat is a supergroup or a channel.
     */
    val messageId: Long? = null,
    /**
     * Options used for link preview generation for the original message, if it is a text message.
     */
    val linkPreviewOptions: TelegramLinkPreviewOptions? = null,
    /**
     * Message is an animation, information about the animation.
     */
    val animation: TelegramAnimation? = null,
    /**
     * Message is an audio file, information about the file.
     */
    val audio: TelegramAudio? = null,
    /**
     * Message is a general file, information about the file.
     */
    val document: TelegramDocument? = null,
    /**
     * Message contains paid media; information about the paid media.
     */
    val paidMedia: TelegramPaidMediaInfo? = null,
    /**
     * Message is a photo, available sizes of the photo.
     */
    val photo: List<TelegramPhotoSize>? = null,
    /**
     * Message is a sticker, information about the sticker.
     */
    val sticker: TelegramSticker? = null,
    /**
     * Message is a forwarded story.
     */
    val story: TelegramStory? = null,
    /**
     * Message is a video, information about the video.
     */
    val video: TelegramVideo? = null,
    /**
     * Message is a [video note](https://telegram.org/blog/video-messages-and-telescope),
     * information about the video message.
     */
    val videoNote: TelegramVideoNote? = null,
    /**
     * Message is a voice message, information about the file.
     */
    val voice: TelegramVoice? = null,
    /**
     * True, if the message media is covered by a spoiler animation.
     */
    val hasMediaSpoiler: Boolean = false,
    /**
     * Message is a shared contact, information about the contact.
     */
    val contact: TelegramContact? = null,
    /**
     * Message is a die with random value.
     */
    val dice: TelegramDice? = null,
    /**
     * Message is a game, information about the game.
     */
    val game: TelegramGame? = null,
    /**
     * Message is a scheduled giveaway, information about the giveaway.
     */
    val giveaway: TelegramGiveaway? = null,
    /**
     * A giveaway with public winners was completed.
     */
    val giveawayWinners: TelegramGiveawayWinners? = null,
    /**
     * Message is an invoice for a [payment](https://core.telegram.org/bots/api#payments),
     * information about the invoice.
     */
    val invoice: TelegramInvoice? = null,
    /**
     * Message is a shared location, information about the location.
     */
    val location: TelegramLocation? = null,
    /**
     * Message is a native poll, information about the poll.
     */
    val poll: TelegramPoll? = null,
    /**
     * Message is a venue, information about the venue.
     */
    val venue: TelegramVenue? = null,
)