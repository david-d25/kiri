package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramForumTopicEdited
import space.davids_digital.kiri.orm.entity.telegram.TelegramForumTopicEditedEntity

@Mapper(componentModel = "spring")
interface TelegramForumTopicEditedEntityMapper {
    fun toEntity(model: TelegramForumTopicEdited): TelegramForumTopicEditedEntity
    fun toModel(entity: TelegramForumTopicEditedEntity): TelegramForumTopicEdited
}
