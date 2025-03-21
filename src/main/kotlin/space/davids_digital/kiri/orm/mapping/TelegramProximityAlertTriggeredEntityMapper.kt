package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramProximityAlertTriggered
import space.davids_digital.kiri.orm.entity.telegram.TelegramProximityAlertTriggeredEntity

@Mapper(componentModel = "spring")
interface TelegramProximityAlertTriggeredEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramProximityAlertTriggered): TelegramProximityAlertTriggeredEntity
    fun toModel(entity: TelegramProximityAlertTriggeredEntity): TelegramProximityAlertTriggered
}
