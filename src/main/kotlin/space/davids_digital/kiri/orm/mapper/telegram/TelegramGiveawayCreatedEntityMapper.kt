package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramGiveawayCreated
import space.davids_digital.kiri.orm.entity.telegram.TelegramGiveawayCreatedEntity

@Mapper
interface TelegramGiveawayCreatedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramGiveawayCreated?): TelegramGiveawayCreatedEntity?

    @Mapping(target = "copy", ignore = true)
    fun toModel(entity: TelegramGiveawayCreatedEntity?): TelegramGiveawayCreated?
}
