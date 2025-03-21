package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Named
import space.davids_digital.kiri.model.telegram.TelegramBackgroundType
import space.davids_digital.kiri.model.telegram.TelegramBackgroundType.*
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [TelegramBackgroundFillEntityMapper::class, TelegramDocumentEntityMapper::class]
)
interface TelegramBackgroundTypeEntityMapper {
    @Named("toEntityFill")
    fun toEntity(model: Fill): TelegramBackgroundTypeFillEntity

    @Named("toEntityWallpaper")
    fun toEntity(model: Wallpaper): TelegramBackgroundTypeWallpaperEntity

    @Named("toEntityPattern")
    fun toEntity(model: Pattern): TelegramBackgroundTypePatternEntity

    @Named("toEntityChatTheme")
    fun toEntity(model: ChatTheme): TelegramBackgroundTypeChatThemeEntity

    @Named("toEntityUnknown")
    fun toEntity(model: Unknown): TelegramBackgroundTypeUnknownEntity

    fun toEntity(model: TelegramBackgroundType): TelegramBackgroundTypeEntity {
        return when (model) {
            is Fill -> toEntity(model)
            is Wallpaper -> toEntity(model)
            is Pattern -> toEntity(model)
            is ChatTheme -> toEntity(model)
            is Unknown -> toEntity(model)
        }
    }

    @Named("toModelFill")
    fun toModel(entity: TelegramBackgroundTypeFillEntity): Fill

    @Named("toModelWallpaper")
    fun toModel(entity: TelegramBackgroundTypeWallpaperEntity): Wallpaper

    @Named("toModelPattern")
    fun toModel(entity: TelegramBackgroundTypePatternEntity): Pattern

    @Named("toModelChatTheme")
    fun toModel(entity: TelegramBackgroundTypeChatThemeEntity): ChatTheme

    @Named("toModelUnknown")
    fun toModel(entity: TelegramBackgroundTypeUnknownEntity): Unknown

    fun toModel(entity: TelegramBackgroundTypeEntity): TelegramBackgroundType {
        return when (entity) {
            is TelegramBackgroundTypeFillEntity -> toModel(entity)
            is TelegramBackgroundTypeWallpaperEntity -> toModel(entity)
            is TelegramBackgroundTypePatternEntity -> toModel(entity)
            is TelegramBackgroundTypeChatThemeEntity -> toModel(entity)
            is TelegramBackgroundTypeUnknownEntity -> toModel(entity)
            else -> error("Unknown TelegramBackgroundTypeEntity: ${entity::class.qualifiedName}")
        }
    }
}
