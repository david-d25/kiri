package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramSticker
import space.davids_digital.kiri.orm.entity.telegram.TelegramStickerEntity

@Mapper
interface TelegramStickerEntityTypeMapper {
    fun toModel(entity: TelegramStickerEntity.Type): TelegramSticker.Type
    fun toEntity(model: TelegramSticker.Type): TelegramStickerEntity.Type
}