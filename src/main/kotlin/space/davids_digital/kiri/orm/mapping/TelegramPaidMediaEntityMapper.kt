package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramPaidMedia
import space.davids_digital.kiri.model.telegram.TelegramPaidMedia.*
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaEntity
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaPhotoEntity
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaPreviewEntity
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaUnknownEntity
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaVideoEntity

@Mapper(componentModel = "spring", uses = [TelegramPhotoSizeEntityMapper::class, TelegramVideoEntityMapper::class])
interface TelegramPaidMediaEntityMapper {
    fun toEntity(model: Preview): TelegramPaidMediaPreviewEntity
    fun toEntity(model: Photo): TelegramPaidMediaPhotoEntity
    fun toEntity(model: Video): TelegramPaidMediaVideoEntity
    fun toEntity(model: Unknown): TelegramPaidMediaUnknownEntity
    fun toEntity(model: TelegramPaidMedia): TelegramPaidMediaEntity {
        return when (model) {
            is Preview -> toEntity(model)
            is Photo -> toEntity(model)
            is Video -> toEntity(model)
            is Unknown -> toEntity(model)
        }
    }

    fun toModel(entity: TelegramPaidMediaPreviewEntity): Preview
    fun toModel(entity: TelegramPaidMediaPhotoEntity): Photo
    fun toModel(entity: TelegramPaidMediaVideoEntity): Video
    fun toModel(entity: TelegramPaidMediaUnknownEntity): Unknown
    fun toModel(entity: TelegramPaidMediaEntity): TelegramPaidMedia {
        return when (entity) {
            is TelegramPaidMediaPreviewEntity -> toModel(entity)
            is TelegramPaidMediaPhotoEntity -> toModel(entity)
            is TelegramPaidMediaVideoEntity -> toModel(entity)
            is TelegramPaidMediaUnknownEntity -> toModel(entity)
            else -> throw IllegalArgumentException("Unknown entity type: ${entity::class.simpleName}")
        }
    }
}