package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramLocation
import space.davids_digital.kiri.orm.entity.telegram.TelegramLocationEntity

@Mapper(componentModel = "spring")
interface TelegramLocationEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramLocation?): TelegramLocationEntity?
    fun toModel(entity: TelegramLocationEntity?): TelegramLocation?
}
