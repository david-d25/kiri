package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramMaskPosition
import space.davids_digital.kiri.orm.entity.telegram.TelegramMaskPositionEntity

@Mapper
interface TelegramMaskPositionEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramMaskPosition?): TelegramMaskPositionEntity?

    @Mapping(source = "XShift", target = "xShift")
    @Mapping(source = "YShift", target = "yShift")
    fun toModel(entity: TelegramMaskPositionEntity?): TelegramMaskPosition?
}
