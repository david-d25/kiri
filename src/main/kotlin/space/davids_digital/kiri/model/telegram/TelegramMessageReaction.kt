package space.davids_digital.kiri.model.telegram

/**
 * A single reaction on a message together with how many times it was added. This is a flattened, persistence-friendly
 * representation of [TelegramReactionType] + count, used to store the aggregate reactions of a message.
 */
data class TelegramMessageReaction(
    /**
     * Standard emoji of the reaction (e.g. "👍"), if it is an emoji reaction.
     */
    val emoji: String? = null,

    /**
     * Custom emoji identifier, if it is a custom (premium) emoji reaction.
     */
    val customEmojiId: String? = null,

    /**
     * For custom emoji reactions, the standard emoji the custom emoji is based on. This is only an approximation:
     * the actual premium emoji shown in the Telegram UI may look different from this fallback.
     */
    val fallbackEmoji: String? = null,

    /**
     * True if it is a paid (Telegram Stars) reaction.
     */
    val paid: Boolean = false,

    /**
     * Number of times this reaction was added.
     */
    val count: Int = 0,
)
