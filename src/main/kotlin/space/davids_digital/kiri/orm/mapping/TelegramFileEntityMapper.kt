package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramFile
import space.davids_digital.kiri.orm.entity.telegram.TelegramFileEntity

@Mapper(componentModel = "spring")
interface TelegramFileEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    fun toEntity(model: TelegramFile?): TelegramFileEntity?

    @Mapping(source = "fileDownloadId", target = "fileId")
    fun toModel(entity: TelegramFileEntity?): TelegramFile?
}
