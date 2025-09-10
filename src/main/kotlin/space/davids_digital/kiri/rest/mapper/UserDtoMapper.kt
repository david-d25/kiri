package space.davids_digital.kiri.rest.mapper

import org.mapstruct.AnnotateWith
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.springframework.context.annotation.Primary
import space.davids_digital.kiri.model.User
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.rest.dto.UserDto
import space.davids_digital.kiri.rest.mapper.telegram.TelegramUserDtoMapper

@Mapper(uses = [TelegramUserDtoMapper::class])
@AnnotateWith(Primary::class)
interface UserDtoMapper {
    @Mapping(target = "smallPhotoUrl", source = "smallPhotoUrl")
    @Mapping(target = "bigPhotoUrl", source = "bigPhotoUrl")
    @Mapping(target = "telegramUser", source = "telegramUser")
    @Mapping(target = "id", source = "model.id")
    fun toDto(model: User?, telegramUser: TelegramUser, smallPhotoUrl: String?, bigPhotoUrl: String?): UserDto?

    fun toDto(model: User.Role): UserDto.Role
    fun toModel(dto: UserDto.Role): User.Role
}