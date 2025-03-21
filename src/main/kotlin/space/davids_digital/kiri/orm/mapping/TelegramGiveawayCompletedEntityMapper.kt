package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramGiveawayCompleted
import space.davids_digital.kiri.orm.entity.telegram.TelegramGiveawayCompletedEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramMessageEntityMapper::class]
)
interface TelegramGiveawayCompletedEntityMapper {
    fun toEntity(model: TelegramGiveawayCompleted): TelegramGiveawayCompletedEntity
    fun toModel(entity: TelegramGiveawayCompletedEntity): TelegramGiveawayCompleted
}
