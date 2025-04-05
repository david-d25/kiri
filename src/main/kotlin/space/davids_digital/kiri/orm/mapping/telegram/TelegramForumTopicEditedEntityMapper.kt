package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramForumTopicEdited
import space.davids_digital.kiri.orm.entity.telegram.TelegramForumTopicEditedEntity

@Mapper(componentModel = "spring")
interface TelegramForumTopicEditedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramForumTopicEdited?): TelegramForumTopicEditedEntity?
    fun toModel(entity: TelegramForumTopicEditedEntity?): TelegramForumTopicEdited?
}
