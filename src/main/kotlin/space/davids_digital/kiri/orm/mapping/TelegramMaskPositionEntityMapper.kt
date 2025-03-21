package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramMaskPosition
import space.davids_digital.kiri.orm.entity.telegram.TelegramMaskPositionEntity

@Mapper(componentModel = "spring")
interface TelegramMaskPositionEntityMapper {
    fun toEntity(model: TelegramMaskPosition): TelegramMaskPositionEntity
    fun toModel(entity: TelegramMaskPositionEntity): TelegramMaskPosition
}
