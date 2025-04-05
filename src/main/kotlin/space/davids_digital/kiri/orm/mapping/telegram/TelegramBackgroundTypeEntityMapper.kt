package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramBackgroundType
import space.davids_digital.kiri.model.telegram.TelegramBackgroundType.*
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [TelegramBackgroundFillEntityMapper::class, TelegramDocumentEntityMapper::class]
)
abstract class TelegramBackgroundTypeEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: Fill?): TelegramBackgroundTypeFillEntity?

    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: Wallpaper?): TelegramBackgroundTypeWallpaperEntity?

    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: Pattern?): TelegramBackgroundTypePatternEntity?

    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: ChatTheme?): TelegramBackgroundTypeChatThemeEntity?

    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: Unknown?): TelegramBackgroundTypeUnknownEntity?

    fun toEntity(model: TelegramBackgroundType?): TelegramBackgroundTypeEntity? {
        return when (model) {
            null -> null
            is Fill -> toEntity(model)
            is Wallpaper -> toEntity(model)
            is Pattern -> toEntity(model)
            is ChatTheme -> toEntity(model)
            is Unknown -> toEntity(model)
        }
    }

    abstract fun toModel(entity: TelegramBackgroundTypeFillEntity?): Fill?

    @Mapping(source = "blurred", target = "isBlurred")
    @Mapping(source = "moving", target = "isMoving")
    abstract fun toModel(entity: TelegramBackgroundTypeWallpaperEntity?): Wallpaper?

    @Mapping(source = "inverted", target = "isInverted")
    @Mapping(source = "moving", target = "isMoving")
    abstract fun toModel(entity: TelegramBackgroundTypePatternEntity?): Pattern?

    @Mapping(target = "copy", ignore = true)
    abstract fun toModel(entity: TelegramBackgroundTypeChatThemeEntity?): ChatTheme?

    abstract fun toModel(entity: TelegramBackgroundTypeUnknownEntity?): Unknown?

    fun toModel(entity: TelegramBackgroundTypeEntity?): TelegramBackgroundType? {
        return when (entity) {
            null -> null
            is TelegramBackgroundTypeFillEntity -> toModel(entity)
            is TelegramBackgroundTypeWallpaperEntity -> toModel(entity)
            is TelegramBackgroundTypePatternEntity -> toModel(entity)
            is TelegramBackgroundTypeChatThemeEntity -> toModel(entity)
            is TelegramBackgroundTypeUnknownEntity -> toModel(entity)
            else -> error("Unknown TelegramBackgroundTypeEntity: ${entity::class.qualifiedName}")
        }
    }
}
