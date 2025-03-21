package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramUsersShared
import space.davids_digital.kiri.orm.entity.telegram.TelegramUsersSharedEntity

@Mapper(componentModel = "spring", uses = [TelegramSharedUserEntityMapper::class])
interface TelegramUsersSharedEntityMapper {
    fun toEntity(model: TelegramUsersShared): TelegramUsersSharedEntity
    fun toModel(entity: TelegramUsersSharedEntity): TelegramUsersShared
}
