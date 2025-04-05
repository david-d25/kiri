package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramChatBoostAdded
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatBoostAddedEntity

@Mapper(componentModel = "spring")
interface TelegramChatBoostAddedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramChatBoostAdded?): TelegramChatBoostAddedEntity?
    fun toModel(entity: TelegramChatBoostAddedEntity?): TelegramChatBoostAdded?
}
