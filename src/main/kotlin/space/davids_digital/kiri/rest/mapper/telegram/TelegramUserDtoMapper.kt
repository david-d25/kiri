package space.davids_digital.kiri.rest.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.rest.dto.telegram.TelegramUserDto

@Mapper
interface TelegramUserDtoMapper {
    @Mapping(target = "isBot", source = "bot")
    @Mapping(target = "isPremium", source = "premium")
    fun toDto(model: TelegramUser?): TelegramUserDto?
}