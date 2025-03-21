package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramWebAppInfo
import space.davids_digital.kiri.orm.entity.telegram.TelegramWebAppInfoEntity

@Mapper(componentModel = "spring")
interface TelegramWebAppInfoEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramWebAppInfo): TelegramWebAppInfoEntity

    fun toModel(entity: TelegramWebAppInfoEntity): TelegramWebAppInfo
}
