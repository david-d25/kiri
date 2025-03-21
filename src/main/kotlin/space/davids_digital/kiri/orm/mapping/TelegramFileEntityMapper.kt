package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramFile
import space.davids_digital.kiri.orm.entity.telegram.TelegramFileEntity

@Mapper(componentModel = "spring")
interface TelegramFileEntityMapper {
    fun toEntity(model: TelegramFile): TelegramFileEntity
    fun toModel(entity: TelegramFileEntity): TelegramFile
}
