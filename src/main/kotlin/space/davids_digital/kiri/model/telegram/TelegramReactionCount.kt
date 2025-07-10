package space.davids_digital.kiri.model.telegram

/**
 * Represents a reaction added to a message along with the number of times it was added.
 */
data class TelegramReactionCount (
    /**
     * Type of the reaction
     */
    val type: TelegramReactionType,

    /**
     * Number of times the reaction was added
     */
    val totalCount: Int = 0,
)