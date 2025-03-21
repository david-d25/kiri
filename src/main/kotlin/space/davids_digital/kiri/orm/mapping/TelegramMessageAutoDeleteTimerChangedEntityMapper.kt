package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramMessageAutoDeleteTimerChanged
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageAutoDeleteTimerChangedEntity

@Mapper(componentModel = "spring")
interface TelegramMessageAutoDeleteTimerChangedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramMessageAutoDeleteTimerChanged): TelegramMessageAutoDeleteTimerChangedEntity

    @Mapping(target = "copy", ignore = true)
    fun toModel(entity: TelegramMessageAutoDeleteTimerChangedEntity): TelegramMessageAutoDeleteTimerChanged
}
