package space.davids_digital.kiri.rest.dto

data class BootstrapDto (
    val isAuthenticated: Boolean,
    val login: LoginInfo? = null,
    val user: UserInfo? = null,
) {
    init {
        require(login != null || user != null) { "Either 'login' or 'user' must be present" }
    }

    data class LoginInfo(
        val telegramBotUsername: String,
        val telegramAuthCallbackUrl: String,
    )
    data class UserInfo(
        val id: Long,
        val isAdmin: Boolean,
        val firstName: String,
        val lastName: String?,
        val username: String?,
        val photoUrl: String?,
    )
}