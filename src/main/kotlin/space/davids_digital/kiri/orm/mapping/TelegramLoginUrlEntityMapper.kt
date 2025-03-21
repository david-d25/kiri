package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramLoginUrl
import space.davids_digital.kiri.orm.entity.telegram.TelegramLoginUrlEntity

@Mapper(componentModel = "spring")
interface TelegramLoginUrlEntityMapper {
    fun toEntity(model: TelegramLoginUrl): TelegramLoginUrlEntity
    fun toModel(entity: TelegramLoginUrlEntity): TelegramLoginUrl
}
