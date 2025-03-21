package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramChatBoostAdded
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatBoostAddedEntity

@Mapper(componentModel = "spring")
interface TelegramChatBoostAddedEntityMapper {
    fun toEntity(model: TelegramChatBoostAdded): TelegramChatBoostAddedEntity
    fun toModel(entity: TelegramChatBoostAddedEntity): TelegramChatBoostAdded
}
