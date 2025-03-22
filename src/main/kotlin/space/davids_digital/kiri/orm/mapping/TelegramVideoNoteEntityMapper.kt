package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramVideoNote
import space.davids_digital.kiri.orm.entity.telegram.TelegramVideoNoteEntity

@Mapper(componentModel = "spring", uses = [TelegramPhotoSizeEntityMapper::class])
interface TelegramVideoNoteEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    fun toEntity(model: TelegramVideoNote?): TelegramVideoNoteEntity?

    @Mapping(source = "fileDownloadId", target = "fileId")
    fun toModel(entity: TelegramVideoNoteEntity?): TelegramVideoNote?
}
