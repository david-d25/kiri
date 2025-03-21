package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.entity.telegram.TelegramUserEntity

@Mapper(componentModel = "spring")
interface TelegramUserEntityMapper {
    fun toEntity(model: TelegramUser): TelegramUserEntity
    fun toModel(entity: TelegramUserEntity): TelegramUser
}