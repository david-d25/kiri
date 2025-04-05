package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramOrderInfo
import space.davids_digital.kiri.orm.entity.telegram.TelegramOrderInfoEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramShippingAddressEntityMapper::class]
)
interface TelegramOrderInfoEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramOrderInfo?): TelegramOrderInfoEntity?
    fun toModel(entity: TelegramOrderInfoEntity?): TelegramOrderInfo?
}
