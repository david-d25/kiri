package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramRefundedPayment
import space.davids_digital.kiri.orm.entity.telegram.TelegramRefundedPaymentEntity

@Mapper(componentModel = "spring")
interface TelegramRefundedPaymentEntityMapper {
    fun toEntity(model: TelegramRefundedPayment): TelegramRefundedPaymentEntity
    fun toModel(entity: TelegramRefundedPaymentEntity): TelegramRefundedPayment
}
