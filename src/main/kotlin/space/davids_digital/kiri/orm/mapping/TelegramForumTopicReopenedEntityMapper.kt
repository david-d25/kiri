package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramForumTopicReopened

@Mapper(componentModel = "spring")
abstract class TelegramForumTopicReopenedEntityMapper {
    fun toEntity(model: TelegramForumTopicReopened?): Boolean = model != null
    fun toModel(entity: Boolean): TelegramForumTopicReopened? = if (entity) TelegramForumTopicReopened() else null
}