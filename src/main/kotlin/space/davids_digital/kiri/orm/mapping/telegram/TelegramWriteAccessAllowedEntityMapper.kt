package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramWriteAccessAllowed
import space.davids_digital.kiri.orm.entity.telegram.TelegramWriteAccessAllowedEntity

@Mapper(componentModel = "spring")
interface TelegramWriteAccessAllowedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramWriteAccessAllowed?): TelegramWriteAccessAllowedEntity?
    fun toModel(entity: TelegramWriteAccessAllowedEntity?): TelegramWriteAccessAllowed?
}
