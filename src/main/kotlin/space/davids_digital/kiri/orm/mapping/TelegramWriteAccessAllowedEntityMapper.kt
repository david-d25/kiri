package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramWriteAccessAllowed
import space.davids_digital.kiri.orm.entity.telegram.TelegramWriteAccessAllowedEntity

@Mapper(componentModel = "spring")
interface TelegramWriteAccessAllowedEntityMapper {
    fun toEntity(model: TelegramWriteAccessAllowed): TelegramWriteAccessAllowedEntity
    fun toModel(entity: TelegramWriteAccessAllowedEntity): TelegramWriteAccessAllowed
}
