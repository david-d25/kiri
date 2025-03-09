package space.davids_digital.kiri.model.telegram

data class TelegramPollOption (
    /**
     * Option text, 1-100 characters.
     */
    val text: String,
    /**
     * Number of users that voted for this option.
     */
    val voterCount: Int,
    /**
     * Special entities that appear in the option text.
     * Currently, only custom emoji entities are allowed in poll option texts.
     */
    val textEntities: Array<TelegramMessageEntity>? = null,
)