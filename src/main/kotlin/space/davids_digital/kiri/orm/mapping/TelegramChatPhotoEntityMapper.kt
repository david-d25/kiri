package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramChatPhoto
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatPhotoEntity

@Mapper(componentModel = "spring")
interface TelegramChatPhotoEntityMapper {
    @Mapping(source = "smallFileUniqueId", target = "smallFileId")
    @Mapping(source = "bigFileUniqueId", target = "bigFileId")
    fun toEntity(model: TelegramChatPhoto): TelegramChatPhotoEntity

    @Mapping(source = "smallFileId", target = "smallFileUniqueId")
    @Mapping(source = "bigFileId", target = "bigFileUniqueId")
    fun toModel(entity: TelegramChatPhotoEntity): TelegramChatPhoto
}
