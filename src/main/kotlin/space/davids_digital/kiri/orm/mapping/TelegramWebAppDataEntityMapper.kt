package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramWebAppData
import space.davids_digital.kiri.orm.entity.telegram.TelegramWebAppDataEntity

@Mapper(componentModel = "spring")
interface TelegramWebAppDataEntityMapper {
    fun toEntity(model: TelegramWebAppData): TelegramWebAppDataEntity
    fun toModel(entity: TelegramWebAppDataEntity): TelegramWebAppData
}
