package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramVideoChatEnded
import space.davids_digital.kiri.orm.entity.telegram.TelegramVideoChatEndedEntity

@Mapper(componentModel = "spring")
interface TelegramVideoChatEndedEntityMapper {
    fun toEntity(model: TelegramVideoChatEnded): TelegramVideoChatEndedEntity
    fun toModel(entity: TelegramVideoChatEndedEntity): TelegramVideoChatEnded
}
