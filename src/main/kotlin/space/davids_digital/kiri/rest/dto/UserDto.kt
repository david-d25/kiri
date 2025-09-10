package space.davids_digital.kiri.rest.dto

import space.davids_digital.kiri.rest.dto.telegram.TelegramUserDto

data class UserDto (
    val id: Long,
    val telegramUser: TelegramUserDto,
    val role: Role,
    val smallPhotoUrl: String?,
    val bigPhotoUrl: String?
) {
    enum class Role {
        OWNER,
        ADMIN
    }
}