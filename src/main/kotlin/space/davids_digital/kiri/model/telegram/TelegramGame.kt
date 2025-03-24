package space.davids_digital.kiri.model.telegram

/**
 * Represents a game. Use BotFather to create and edit games, their short names will act as unique identifiers.
 */
data class TelegramGame (
    /**
     * Title of the game.
     */
    val title: String,
    /**
     * Description of the game.
     */
    val description: String,
    /**
     * Photo that will be displayed in the game message in chats.
     */
    val photo: List<TelegramPhotoSize>,
    /**
     * Brief description of the game or high scores included in the game message.
     * Can be automatically edited to include current high scores for the game when the bot calls
     * [setGameScore](https://core.telegram.org/bots/api#setgamescore), or manually edited using
     * [editMessageText](https://core.telegram.org/bots/api#editmessagetext). 0-4096 characters.
     */
    val text: String? = null,
    /**
     * Special entities that appear in [text], such as usernames, URLs, bot commands, etc.
     */
    val textEntities: List<TelegramMessageEntity> = emptyList(),
    /**
     * Animation that will be displayed in the game message in chats. Upload via [BotFather](https://t.me/botfather).
     */
    val animation: TelegramAnimation? = null,
)