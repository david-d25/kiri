package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramChatPermissions
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatPermissionsEntity

@Mapper
interface TelegramChatPermissionsEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramChatPermissions?): TelegramChatPermissionsEntity?
    fun toModel(entity: TelegramChatPermissionsEntity?): TelegramChatPermissions?
}
