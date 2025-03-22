package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramWebAppData
import space.davids_digital.kiri.orm.entity.telegram.TelegramWebAppDataEntity

@Mapper(componentModel = "spring")
interface TelegramWebAppDataEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramWebAppData?): TelegramWebAppDataEntity?
    fun toModel(entity: TelegramWebAppDataEntity?): TelegramWebAppData?
}
