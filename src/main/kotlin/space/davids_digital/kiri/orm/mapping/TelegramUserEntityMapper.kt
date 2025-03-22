package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.entity.telegram.TelegramUserEntity

@Mapper(componentModel = "spring")
interface TelegramUserEntityMapper {
    fun toEntity(model: TelegramUser?): TelegramUserEntity?

    @Mapping(source = "bot", target = "isBot")
    @Mapping(source = "premium", target = "isPremium")
    fun toModel(entity: TelegramUserEntity?): TelegramUser?
}