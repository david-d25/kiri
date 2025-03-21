package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramChatShared
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatSharedEntity

@Mapper(componentModel = "spring", uses = [TelegramPhotoSizeEntityMapper::class])
interface TelegramChatSharedEntityMapper {
    fun toEntity(model: TelegramChatShared): TelegramChatSharedEntity
    fun toModel(entity: TelegramChatSharedEntity): TelegramChatShared
}
