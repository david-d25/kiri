package space.davids_digital.kiri.model.telegram

/**
 * Describes the type of reaction.
 */
sealed class TelegramReactionType {
    /**
     * The reaction is based on an emoji.
     */
    data class Emoji(
        /**
         * 	Reaction emoji. Currently, it can be one of "❤", "👍", "👎", "🔥", "🥰", "👏", "😁", "🤔", "🤯", "😱",
         * 	"🤬", "😢", "🎉", "🤩", "🤮", "💩", "🙏", "👌", "🕊", "🤡", "🥱", "🥴", "😍", "🐳", "❤‍🔥", "🌚", "🌭",
         * 	"💯", "🤣", "⚡", "🍌", "🏆", "💔", "🤨", "😐", "🍓", "🍾", "💋", "🖕", "😈", "😴", "😭", "🤓", "👻",
         * 	"👨‍💻", "👀", "🎃", "🙈", "😇", "😨", "🤝", "✍", "🤗", "🫡", "🎅", "🎄", "☃", "💅", "🤪", "🗿", "🆒",
         * 	"💘", "🙉", "🦄", "😘", "💊", "🙊", "😎", "👾", "🤷‍♂", "🤷", "🤷‍♀", "😡"
         */
        val emoji: String,
    ) : TelegramReactionType()

    data class CustomEmoji(
        /**
         * Custom emoji identifier
         */
        val customEmojiId: String,
    ) : TelegramReactionType()

    class Paid : TelegramReactionType()
}