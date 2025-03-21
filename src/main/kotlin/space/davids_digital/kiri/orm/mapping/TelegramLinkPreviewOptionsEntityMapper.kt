package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramLinkPreviewOptions
import space.davids_digital.kiri.orm.entity.telegram.TelegramLinkPreviewOptionsEntity

@Mapper(componentModel = "spring")
interface TelegramLinkPreviewOptionsEntityMapper {
    fun toEntity(model: TelegramLinkPreviewOptions): TelegramLinkPreviewOptionsEntity
    fun toModel(entity: TelegramLinkPreviewOptionsEntity): TelegramLinkPreviewOptions
}
