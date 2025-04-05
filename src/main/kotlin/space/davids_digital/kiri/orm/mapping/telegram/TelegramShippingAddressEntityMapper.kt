package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramShippingAddress
import space.davids_digital.kiri.orm.entity.telegram.TelegramShippingAddressEntity

@Mapper(componentModel = "spring")
interface TelegramShippingAddressEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramShippingAddress?): TelegramShippingAddressEntity?
    fun toModel(entity: TelegramShippingAddressEntity?): TelegramShippingAddress?
}
