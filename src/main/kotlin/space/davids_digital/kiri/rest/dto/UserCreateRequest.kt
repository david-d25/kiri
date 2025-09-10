package space.davids_digital.kiri.rest.dto

data class UserCreateRequest(
    val telegramUserId: Long,
    val role: UserDto.Role
)