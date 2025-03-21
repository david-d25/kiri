package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import space.davids_digital.kiri.model.telegram.TelegramSticker
import space.davids_digital.kiri.model.telegram.TelegramSticker.Type
import space.davids_digital.kiri.orm.entity.telegram.TelegramStickerEntity

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramPhotoSizeEntityMapper::class,
        TelegramMaskPositionEntityMapper::class,
        TelegramFileEntityMapper::class
    ]
)
interface TelegramStickerEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    @Mapping(source = "type", target = "type", qualifiedByName = ["enumToString"])
    fun toEntity(model: TelegramSticker): TelegramStickerEntity

    @Mapping(source = "fileDownloadId", target = "fileId")
    @Mapping(source = "type", target = "type", qualifiedByName = ["stringToEnum"])
    fun toModel(entity: TelegramStickerEntity): TelegramSticker

    @Named("enumToString")
    fun enumToString(type: Type): String = type.name.lowercase()

    @Named("stringToEnum")
    fun stringToEnum(type: String): Type = try {
        Type.valueOf(type.uppercase())
    } catch (e: Exception) {
        Type.UNKNOWN
    }
}
