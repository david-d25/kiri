package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramPhotoSize
import space.davids_digital.kiri.orm.entity.telegram.TelegramPhotoSizeEntity

@Mapper
interface TelegramPhotoSizeEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    fun toEntity(model: TelegramPhotoSize?): TelegramPhotoSizeEntity?

    @Mapping(source = "fileDownloadId", target = "fileId")
    fun toModel(entity: TelegramPhotoSizeEntity?): TelegramPhotoSize?
}
