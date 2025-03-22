package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramInvoice
import space.davids_digital.kiri.orm.entity.telegram.TelegramInvoiceEntity

@Mapper(componentModel = "spring")
interface TelegramInvoiceEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramInvoice?): TelegramInvoiceEntity?
    fun toModel(entity: TelegramInvoiceEntity?): TelegramInvoice?
}
