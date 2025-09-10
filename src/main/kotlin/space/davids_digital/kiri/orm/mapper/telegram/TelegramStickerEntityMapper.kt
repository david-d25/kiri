package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramSticker
import space.davids_digital.kiri.orm.entity.telegram.TelegramStickerEntity

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramPhotoSizeEntityMapper::class,
        TelegramMaskPositionEntityMapper::class,
        TelegramFileEntityMapper::class,
        TelegramStickerEntityTypeMapper::class,
    ]
)
interface TelegramStickerEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    fun toEntity(model: TelegramSticker?): TelegramStickerEntity?

    @Mapping(source = "fileDownloadId", target = "fileId")
    @Mapping(source = "animated", target = "isAnimated")
    @Mapping(source = "video", target = "isVideo")
    fun toModel(entity: TelegramStickerEntity?): TelegramSticker?
}
