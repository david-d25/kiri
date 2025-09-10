package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramVideoChatScheduled
import space.davids_digital.kiri.orm.entity.telegram.TelegramVideoChatScheduledEntity
import space.davids_digital.kiri.orm.mapper.DateTimeMapper

@Mapper(
    componentModel = "spring",
    uses = [DateTimeMapper::class]
)
interface TelegramVideoChatScheduledEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramVideoChatScheduled?): TelegramVideoChatScheduledEntity?

    @Mapping(target = "copy", ignore = true)
    fun toModel(entity: TelegramVideoChatScheduledEntity?): TelegramVideoChatScheduled?
}
