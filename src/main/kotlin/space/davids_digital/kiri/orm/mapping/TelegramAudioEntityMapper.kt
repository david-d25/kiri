package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramAudio
import space.davids_digital.kiri.orm.entity.telegram.TelegramAudioEntity

@Mapper(componentModel = "spring", uses = [TelegramPhotoSizeEntityMapper::class])
interface TelegramAudioEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    fun toEntity(model: TelegramAudio?): TelegramAudioEntity?

    @Mapping(source = "fileDownloadId", target = "fileId")
    fun toModel(entity: TelegramAudioEntity?): TelegramAudio?
}
