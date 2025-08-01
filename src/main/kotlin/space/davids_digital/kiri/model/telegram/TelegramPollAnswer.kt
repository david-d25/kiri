package space.davids_digital.kiri.model.telegram

/**
 * This object represents an answer of a user in a non-anonymous poll.
 */
data class TelegramPollAnswer(
    /**
     * Unique poll identifier
     */
    val pollId: String,

    /**
     * The chat that changed the answer to the poll, if the voter is anonymous
     */
    val voterChat: TelegramChat? = null,

    /**
     * The user that changed the answer to the poll, if the voter isn't anonymous
     */
    val user: TelegramUser? = null,

    /**
     * 0-based identifiers of chosen answer options. May be empty if the vote was retracted.
     */
    val optionIds: List<Int> = emptyList()
)
