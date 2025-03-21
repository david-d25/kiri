package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramPassportFile
import space.davids_digital.kiri.orm.entity.telegram.TelegramPassportFileEntity

@Mapper(componentModel = "spring", uses = [DateTimeMapper::class])
interface TelegramPassportFileEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    fun toEntity(model: TelegramPassportFile): TelegramPassportFileEntity

    @Mapping(source = "fileDownloadId", target = "fileId")
    fun toModel(entity: TelegramPassportFileEntity): TelegramPassportFile
}
