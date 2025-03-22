package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramForumTopicClosed

@Mapper(componentModel = "spring")
abstract class TelegramForumTopicClosedEntityMapper {
    fun toEntity(model: TelegramForumTopicClosed?): Boolean = model != null
    fun toModel(entity: Boolean): TelegramForumTopicClosed? = if (entity) TelegramForumTopicClosed() else null
}