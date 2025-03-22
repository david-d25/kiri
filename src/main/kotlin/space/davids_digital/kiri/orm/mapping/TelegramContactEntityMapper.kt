package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramContact
import space.davids_digital.kiri.orm.entity.telegram.TelegramContactEntity

@Mapper(componentModel = "spring")
interface TelegramContactEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramContact?): TelegramContactEntity?

    fun toModel(entity: TelegramContactEntity?): TelegramContact?
}
