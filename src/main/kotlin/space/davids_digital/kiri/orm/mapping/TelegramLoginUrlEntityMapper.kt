package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramLoginUrl
import space.davids_digital.kiri.orm.entity.telegram.TelegramLoginUrlEntity

@Mapper(componentModel = "spring")
interface TelegramLoginUrlEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramLoginUrl): TelegramLoginUrlEntity
    fun toModel(entity: TelegramLoginUrlEntity): TelegramLoginUrl
}
