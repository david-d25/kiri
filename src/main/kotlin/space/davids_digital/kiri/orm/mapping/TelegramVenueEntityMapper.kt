package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramVenue
import space.davids_digital.kiri.orm.entity.telegram.TelegramVenueEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramLocationEntityMapper::class]
)
interface TelegramVenueEntityMapper {
    fun toEntity(model: TelegramVenue): TelegramVenueEntity
    fun toModel(entity: TelegramVenueEntity): TelegramVenue
}
