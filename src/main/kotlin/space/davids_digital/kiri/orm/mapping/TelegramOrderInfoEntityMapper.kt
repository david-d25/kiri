package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramOrderInfo
import space.davids_digital.kiri.orm.entity.telegram.TelegramOrderInfoEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramShippingAddressEntityMapper::class]
)
interface TelegramOrderInfoEntityMapper {
    fun toEntity(model: TelegramOrderInfo): TelegramOrderInfoEntity
    fun toModel(entity: TelegramOrderInfoEntity): TelegramOrderInfo
}
