package space.davids_digital.kiri.orm.mapping

import space.davids_digital.kiri.model.telegram.TelegramGeneralForumTopicHidden

class TelegramGeneralForumTopicHiddenEntityMapper {
    fun toEntity(model: TelegramGeneralForumTopicHidden?): Boolean = model != null
    fun toModel(entity: Boolean): TelegramGeneralForumTopicHidden? =
        if (entity) TelegramGeneralForumTopicHidden() else null
}