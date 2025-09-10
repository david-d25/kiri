package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramUsersShared
import space.davids_digital.kiri.orm.entity.telegram.TelegramUsersSharedEntity

@Mapper(uses = [TelegramSharedUserEntityMapper::class])
interface TelegramUsersSharedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramUsersShared?): TelegramUsersSharedEntity?
    fun toModel(entity: TelegramUsersSharedEntity?): TelegramUsersShared?
}
