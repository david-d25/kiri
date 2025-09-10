package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramCopyTextButton
import space.davids_digital.kiri.orm.entity.telegram.TelegramCopyTextButtonEntity

@Mapper
interface TelegramCopyTextButtonEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramCopyTextButton?): TelegramCopyTextButtonEntity?

    @Mapping(target = "copy", ignore = true)
    fun toModel(entity: TelegramCopyTextButtonEntity?): TelegramCopyTextButton?
}
