package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramGiveawayCreated
import space.davids_digital.kiri.orm.entity.telegram.TelegramGiveawayCreatedEntity

@Mapper(componentModel = "spring")
interface TelegramGiveawayCreatedEntityMapper {
    fun toEntity(model: TelegramGiveawayCreated): TelegramGiveawayCreatedEntity
    fun toModel(entity: TelegramGiveawayCreatedEntity): TelegramGiveawayCreated
}
