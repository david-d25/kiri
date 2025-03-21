package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramInvoice
import space.davids_digital.kiri.orm.entity.telegram.TelegramInvoiceEntity

@Mapper(componentModel = "spring")
interface TelegramInvoiceEntityMapper {
    fun toEntity(model: TelegramInvoice): TelegramInvoiceEntity
    fun toModel(entity: TelegramInvoiceEntity): TelegramInvoice
}
