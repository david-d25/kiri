package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramVideoChatEnded
import space.davids_digital.kiri.orm.entity.telegram.TelegramVideoChatEndedEntity

@Mapper(componentModel = "spring")
interface TelegramVideoChatEndedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramVideoChatEnded?): TelegramVideoChatEndedEntity?

    @Mapping(target = "copy", ignore = true)
    fun toModel(entity: TelegramVideoChatEndedEntity?): TelegramVideoChatEnded?
}
