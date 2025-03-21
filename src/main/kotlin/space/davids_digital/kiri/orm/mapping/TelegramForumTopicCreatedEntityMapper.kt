package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramForumTopicCreated
import space.davids_digital.kiri.orm.entity.telegram.TelegramForumTopicCreatedEntity
import java.awt.Color

@Mapper(componentModel = "spring")
interface TelegramForumTopicCreatedEntityMapper {
    @Mapping(source = "iconColor", target = "iconColor", qualifiedByName = ["colorToRgb"])
    fun toEntity(model: TelegramForumTopicCreated): TelegramForumTopicCreatedEntity

    @Mapping(source = "iconColor", target = "iconColor", qualifiedByName = ["rgbToColor"])
    fun toModel(entity: TelegramForumTopicCreatedEntity): TelegramForumTopicCreated

    companion object {
        @JvmStatic
        @Named("colorToRgb")
        fun colorToRgb(color: Color): Int = color.rgb

        @JvmStatic
        @Named("rgbToColor")
        fun rgbToColor(rgb: Int): Color = Color(rgb, true)
    }
}
