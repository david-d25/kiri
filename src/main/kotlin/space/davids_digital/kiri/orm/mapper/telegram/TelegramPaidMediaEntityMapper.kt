package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramPaidMedia
import space.davids_digital.kiri.model.telegram.TelegramPaidMedia.*
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaEntity
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaPhotoEntity
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaPreviewEntity
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaUnknownEntity
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaVideoEntity

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramPhotoSizeEntityMapper::class,
        TelegramVideoEntityMapper::class,
        TelegramPaidMediaInfoEntityMapper::class
    ]
)
abstract class TelegramPaidMediaEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "parentPaidMediaInfo", ignore = true)
    abstract fun toEntity(model: Preview?): TelegramPaidMediaPreviewEntity?

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "parentPaidMediaInfo", ignore = true)
    @Mapping(source = "photo", target = "photoSizes")
    abstract fun toEntity(model: Photo?): TelegramPaidMediaPhotoEntity?

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "parentPaidMediaInfo", ignore = true)
    abstract fun toEntity(model: Video?): TelegramPaidMediaVideoEntity?

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "parentPaidMediaInfo", ignore = true)
    abstract fun toEntity(model: Unknown?): TelegramPaidMediaUnknownEntity?

    fun toEntity(model: TelegramPaidMedia?): TelegramPaidMediaEntity? {
        return when (model) {
            null -> null
            is Preview -> toEntity(model)
            is Photo -> toEntity(model)
            is Video -> toEntity(model)
            is Unknown -> toEntity(model)
        }
    }

    abstract fun toModel(entity: TelegramPaidMediaPreviewEntity?): Preview?

    @Mapping(target = "copy", ignore = true)
    @Mapping(source = "photoSizes", target = "photo")
    abstract fun toModel(entity: TelegramPaidMediaPhotoEntity?): Photo?

    @Mapping(target = "copy", ignore = true)
    abstract fun toModel(entity: TelegramPaidMediaVideoEntity?): Video?

    abstract fun toModel(entity: TelegramPaidMediaUnknownEntity?): Unknown?

    fun toModel(entity: TelegramPaidMediaEntity?): TelegramPaidMedia? {
        return when (entity) {
            null -> null
            is TelegramPaidMediaPreviewEntity -> toModel(entity)
            is TelegramPaidMediaPhotoEntity -> toModel(entity)
            is TelegramPaidMediaVideoEntity -> toModel(entity)
            is TelegramPaidMediaUnknownEntity -> toModel(entity)
            else -> throw IllegalArgumentException("Unknown entity type: ${entity::class.simpleName}")
        }
    }
}