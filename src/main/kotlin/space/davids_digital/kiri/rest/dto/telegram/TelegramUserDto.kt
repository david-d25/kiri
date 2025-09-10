package space.davids_digital.kiri.rest.dto.telegram

data class TelegramUserDto (
    val id: Long,
    val isBot: Boolean,
    val firstName: String,
    val lastName: String? = null,
    val username: String? = null,
    val languageCode: String? = null,
    val isPremium: Boolean = false,
    val addedToAttachmentMenu: Boolean = false,
    val canJoinGroups: Boolean = false,
    val canReadAllGroupMessages: Boolean = false,
    val supportsInlineQueries: Boolean = false,
    val canConnectToBusiness: Boolean = false,
    val hasMainWebApp: Boolean = false,
)