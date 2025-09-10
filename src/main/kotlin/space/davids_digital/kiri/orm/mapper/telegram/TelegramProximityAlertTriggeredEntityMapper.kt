package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramProximityAlertTriggered
import space.davids_digital.kiri.orm.entity.telegram.TelegramProximityAlertTriggeredEntity

@Mapper
interface TelegramProximityAlertTriggeredEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramProximityAlertTriggered?): TelegramProximityAlertTriggeredEntity?
    fun toModel(entity: TelegramProximityAlertTriggeredEntity?): TelegramProximityAlertTriggered?
}
