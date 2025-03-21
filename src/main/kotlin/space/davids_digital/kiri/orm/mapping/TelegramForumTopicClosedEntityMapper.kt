package space.davids_digital.kiri.orm.mapping

import space.davids_digital.kiri.model.telegram.TelegramForumTopicClosed

abstract class TelegramForumTopicClosedEntityMapper {
    fun toEntity(model: TelegramForumTopicClosed?): Boolean = model != null
    fun toModel(entity: Boolean): TelegramForumTopicClosed? = if (entity) TelegramForumTopicClosed() else null
}