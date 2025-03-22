package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramStory
import space.davids_digital.kiri.orm.entity.telegram.TelegramStoryEntity

@Mapper(componentModel = "spring")
interface TelegramStoryEntityMapper {
    @Mapping(source = "id", target = "storyId")
    fun toEntity(model: TelegramStory?): TelegramStoryEntity?

    @Mapping(source = "storyId", target = "id")
    fun toModel(entity: TelegramStoryEntity?): TelegramStory?
}
