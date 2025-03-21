package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramWebAppInfo
import space.davids_digital.kiri.orm.entity.telegram.TelegramWebAppInfoEntity

@Mapper(componentModel = "spring")
interface TelegramWebAppInfoEntityMapper {
    fun toEntity(model: TelegramWebAppInfo): TelegramWebAppInfoEntity
    fun toModel(entity: TelegramWebAppInfoEntity): TelegramWebAppInfo
}
