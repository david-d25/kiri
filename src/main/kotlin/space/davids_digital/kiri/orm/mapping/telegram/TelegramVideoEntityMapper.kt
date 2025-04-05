package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramVideo
import space.davids_digital.kiri.orm.entity.telegram.TelegramVideoEntity

@Mapper(componentModel = "spring", uses = [TelegramPhotoSizeEntityMapper::class])
interface TelegramVideoEntityMapper {
    @Mapping(target = "fileDownloadId", source = "fileId")
    fun toEntity(model: TelegramVideo?): TelegramVideoEntity?

    @Mapping(target = "fileId", source = "fileDownloadId")
    fun toModel(entity: TelegramVideoEntity?): TelegramVideo?
}
