package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramVoice
import space.davids_digital.kiri.orm.entity.telegram.TelegramVoiceEntity

@Mapper
interface TelegramVoiceEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    fun toEntity(model: TelegramVoice?): TelegramVoiceEntity?

    @Mapping(source = "fileDownloadId", target = "fileId")
    fun toModel(entity: TelegramVoiceEntity?): TelegramVoice?
}
