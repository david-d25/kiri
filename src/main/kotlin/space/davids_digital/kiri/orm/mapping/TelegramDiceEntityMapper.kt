package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramDice
import space.davids_digital.kiri.orm.entity.telegram.TelegramDiceEntity

@Mapper(componentModel = "spring")
interface TelegramDiceEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramDice?): TelegramDiceEntity?

    fun toModel(entity: TelegramDiceEntity?): TelegramDice?
}
