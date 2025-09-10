package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramGeneralForumTopicUnhidden

@Mapper
abstract class TelegramGeneralForumTopicUnhiddenEntityMapper {
    fun toEntity(model: TelegramGeneralForumTopicUnhidden?): Boolean = model != null
    fun toModel(entity: Boolean): TelegramGeneralForumTopicUnhidden? =
        if (entity) TelegramGeneralForumTopicUnhidden() else null
}