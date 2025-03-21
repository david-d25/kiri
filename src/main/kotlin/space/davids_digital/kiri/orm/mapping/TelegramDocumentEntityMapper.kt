package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramDocument
import space.davids_digital.kiri.orm.entity.telegram.TelegramDocumentEntity

@Mapper(componentModel = "spring", uses = [TelegramPhotoSizeEntityMapper::class])
interface TelegramDocumentEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    fun toEntity(model: TelegramDocument): TelegramDocumentEntity

    @Mapping(source = "fileDownloadId", target = "fileId")
    fun toModel(entity: TelegramDocumentEntity): TelegramDocument
}
