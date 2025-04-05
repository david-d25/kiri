package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramRefundedPayment
import space.davids_digital.kiri.orm.entity.telegram.TelegramRefundedPaymentEntity

@Mapper(componentModel = "spring")
interface TelegramRefundedPaymentEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramRefundedPayment?): TelegramRefundedPaymentEntity?
    fun toModel(entity: TelegramRefundedPaymentEntity?): TelegramRefundedPayment?
}
