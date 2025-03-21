package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramGiveawayCompleted
import space.davids_digital.kiri.orm.entity.telegram.TelegramGiveawayCompletedEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramMessageEntityMapper::class]
)
interface TelegramGiveawayCompletedEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramGiveawayCompleted): TelegramGiveawayCompletedEntity

    @Mapping(source = "starGiveaway", target = "isStarGiveaway")
    fun toModel(entity: TelegramGiveawayCompletedEntity): TelegramGiveawayCompleted
}
