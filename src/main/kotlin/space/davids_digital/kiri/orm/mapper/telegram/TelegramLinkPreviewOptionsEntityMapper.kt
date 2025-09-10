package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramLinkPreviewOptions
import space.davids_digital.kiri.orm.entity.telegram.TelegramLinkPreviewOptionsEntity

@Mapper
interface TelegramLinkPreviewOptionsEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramLinkPreviewOptions?): TelegramLinkPreviewOptionsEntity?
    fun toModel(entity: TelegramLinkPreviewOptionsEntity?): TelegramLinkPreviewOptions?
}
