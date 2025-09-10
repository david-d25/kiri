package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramVideoChatParticipantsInvited
import space.davids_digital.kiri.orm.entity.telegram.TelegramVideoChatParticipantsInvitedEntity

@Mapper
interface TelegramVideoChatParticipantsInvitedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramVideoChatParticipantsInvited?): TelegramVideoChatParticipantsInvitedEntity?

    @Mapping(target = "copy", ignore = true)
    fun toModel(entity: TelegramVideoChatParticipantsInvitedEntity?): TelegramVideoChatParticipantsInvited?
}
