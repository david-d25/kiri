package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramPassportFile
import space.davids_digital.kiri.orm.entity.telegram.TelegramPassportFileEntity
import space.davids_digital.kiri.orm.mapper.DateTimeMapper

@Mapper(uses = [DateTimeMapper::class])
interface TelegramPassportFileEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    fun toEntity(model: TelegramPassportFile?): TelegramPassportFileEntity?

    @Mapping(source = "fileDownloadId", target = "fileId")
    fun toModel(entity: TelegramPassportFileEntity?): TelegramPassportFile?
}
