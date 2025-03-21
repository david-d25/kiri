package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramVideoChatParticipantsInvited
import space.davids_digital.kiri.orm.entity.telegram.TelegramVideoChatParticipantsInvitedEntity

@Mapper(componentModel = "spring")
interface TelegramVideoChatParticipantsInvitedEntityMapper {
    fun toEntity(model: TelegramVideoChatParticipantsInvited): TelegramVideoChatParticipantsInvitedEntity
    fun toModel(entity: TelegramVideoChatParticipantsInvitedEntity): TelegramVideoChatParticipantsInvited
}
