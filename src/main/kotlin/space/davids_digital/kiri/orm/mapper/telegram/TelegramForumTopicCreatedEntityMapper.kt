package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramForumTopicCreated
import space.davids_digital.kiri.orm.entity.telegram.TelegramForumTopicCreatedEntity
import space.davids_digital.kiri.orm.mapper.ColorRgbMapper

@Mapper(uses = [ColorRgbMapper::class])
interface TelegramForumTopicCreatedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramForumTopicCreated?): TelegramForumTopicCreatedEntity?

    fun toModel(entity: TelegramForumTopicCreatedEntity?): TelegramForumTopicCreated?
}
