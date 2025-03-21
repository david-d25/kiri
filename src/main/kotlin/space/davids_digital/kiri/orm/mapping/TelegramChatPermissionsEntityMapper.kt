package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramChatPermissions
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatPermissionsEntity

@Mapper(componentModel = "spring")
interface TelegramChatPermissionsEntityMapper {
    fun toEntity(model: TelegramChatPermissions): TelegramChatPermissionsEntity
    fun toModel(entity: TelegramChatPermissionsEntity): TelegramChatPermissions
}
