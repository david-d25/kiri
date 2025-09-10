package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramVideoChatEnded
import space.davids_digital.kiri.orm.entity.telegram.TelegramVideoChatEndedEntity

@Mapper
interface TelegramVideoChatEndedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramVideoChatEnded?): TelegramVideoChatEndedEntity?

    @Mapping(target = "copy", ignore = true)
    fun toModel(entity: TelegramVideoChatEndedEntity?): TelegramVideoChatEnded?
}
