package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

class TelegramPoll (
    val id: String,
    /**
     * Poll question, 1-300 characters.
     */
    val question: String,
    /**
     * Special entities that appear in the question.
     * Currently, only custom emoji entities are allowed in poll questions.
     */
    val questionEntities: Array<TelegramMessageEntity>? = null,
    val options: Array<TelegramPollOption>,
    val totalVoterCount: Int,
    val isClosed: Boolean,
    val isAnonymous: Boolean,
    val type: Type,
    val allowsMultipleAnswers: Boolean,
    /**
     * 0-based identifier of the correct answer option. Available only for polls in the quiz mode, which are closed, or
     * was sent (not forwarded) by the bot or to the private chat with the bot.
     */
    val correctOptionId: Int? = null,
    /**
     * Text that is shown when a user chooses an incorrect answer or taps on the lamp icon in a quiz-style poll, 0-200
     * characters.
     */
    val explanation: String? = null,
    /**
     * Special entities like usernames, URLs, bot commands, etc. that appear in the explanation.
     */
    val explanationEntities: Array<TelegramMessageEntity>? = null,
    /**
     * Amount of time in seconds the poll will be active after creation.
     */
    val openPeriod: Int? = null,
    /**
     * Point in time when the poll will be automatically closed.
     */
    val closeDate: ZonedDateTime? = null,
) {
    enum class Type {
        REGULAR,
        QUIZ,
    }
}