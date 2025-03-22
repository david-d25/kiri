package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramVideoChatParticipantsInvited
import space.davids_digital.kiri.orm.entity.telegram.TelegramVideoChatParticipantsInvitedEntity

@Mapper(componentModel = "spring")
interface TelegramVideoChatParticipantsInvitedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramVideoChatParticipantsInvited?): TelegramVideoChatParticipantsInvitedEntity?

    @Mapping(target = "copy", ignore = true)
    fun toModel(entity: TelegramVideoChatParticipantsInvitedEntity?): TelegramVideoChatParticipantsInvited?
}
