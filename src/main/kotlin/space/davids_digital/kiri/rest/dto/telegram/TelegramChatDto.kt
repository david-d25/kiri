package space.davids_digital.kiri.rest.dto.telegram

data class TelegramChatDto (
    val id: Long,
    val type: Type,
    val title: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    // TODO
    // val smallPhotoUrl: String? = null,
    // val bigPhotoUrl: String? = null,
    val bio: String? = null,
    val description: String? = null,
    val inviteLink: String? = null,
    val linkedChatId: Long? = null,
) {
    enum class Type {
        PRIVATE,
        GROUP,
        SUPERGROUP,
        CHANNEL
    }
}