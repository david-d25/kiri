package space.davids_digital.kiri.model.telegram

/**
 * Represents an animated emoji that displays a random value.
 *
 * [Reference](https://core.telegram.org/bots/api#dice)
 */
data class TelegramDice (
    /**
     * Emoji on which the dice throw animation is based.
     */
    val emoji: String,
    /**
     * Value of the dice, 1-6 for “🎲”, “🎯” and “🎳” base emoji, 1-5 for “🏀” and “⚽” base emoji, 1-64 for “🎰” base
     * emoji.
     */
    val value: Int,
)