package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramChatShared
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatSharedEntity

@Mapper(componentModel = "spring", uses = [TelegramPhotoSizeEntityMapper::class])
interface TelegramChatSharedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramChatShared?): TelegramChatSharedEntity?
    fun toModel(entity: TelegramChatSharedEntity?): TelegramChatShared?
}
