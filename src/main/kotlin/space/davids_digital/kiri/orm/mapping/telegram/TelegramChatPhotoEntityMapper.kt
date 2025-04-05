package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramChatPhoto
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatPhotoEntity

@Mapper(componentModel = "spring")
interface TelegramChatPhotoEntityMapper {
    @Mapping(source = "smallFileUniqueId", target = "smallFileId")
    @Mapping(source = "bigFileUniqueId", target = "bigFileId")
    @Mapping(source = "smallFileId", target = "smallFileDownloadId")
    @Mapping(source = "bigFileId", target = "bigFileDownloadId")
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramChatPhoto?): TelegramChatPhotoEntity?

    @Mapping(source = "smallFileId", target = "smallFileUniqueId")
    @Mapping(source = "bigFileId", target = "bigFileUniqueId")
    @Mapping(source = "smallFileDownloadId", target = "smallFileId")
    @Mapping(source = "bigFileDownloadId", target = "bigFileId")
    fun toModel(entity: TelegramChatPhotoEntity?): TelegramChatPhoto?
}
