package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramShippingAddress
import space.davids_digital.kiri.orm.entity.telegram.TelegramShippingAddressEntity

@Mapper(componentModel = "spring")
interface TelegramShippingAddressEntityMapper {
    fun toEntity(model: TelegramShippingAddress): TelegramShippingAddressEntity
    fun toModel(entity: TelegramShippingAddressEntity): TelegramShippingAddress
}
