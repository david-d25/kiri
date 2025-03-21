package space.davids_digital.kiri.orm.mapping

import space.davids_digital.kiri.model.telegram.TelegramGeneralForumTopicUnhidden

class TelegramGeneralForumTopicUnhiddenEntityMapper {
    fun toEntity(model: TelegramGeneralForumTopicUnhidden?): Boolean = model != null
    fun toModel(entity: Boolean): TelegramGeneralForumTopicUnhidden? =
        if (entity) TelegramGeneralForumTopicUnhidden() else null
}