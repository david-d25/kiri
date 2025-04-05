package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramVenue
import space.davids_digital.kiri.orm.entity.telegram.TelegramVenueEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramLocationEntityMapper::class]
)
interface TelegramVenueEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramVenue?): TelegramVenueEntity?
    fun toModel(entity: TelegramVenueEntity?): TelegramVenue?
}
