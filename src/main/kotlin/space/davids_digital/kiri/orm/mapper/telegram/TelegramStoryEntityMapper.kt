package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramStory
import space.davids_digital.kiri.orm.entity.telegram.TelegramStoryEntity

@Mapper
interface TelegramStoryEntityMapper {
    @Mapping(source = "id", target = "storyId")
    fun toEntity(model: TelegramStory?): TelegramStoryEntity?

    @Mapping(source = "storyId", target = "id")
    fun toModel(entity: TelegramStoryEntity?): TelegramStory?
}
