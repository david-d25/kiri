package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramCopyTextButton
import space.davids_digital.kiri.orm.entity.telegram.TelegramCopyTextButtonEntity

@Mapper(componentModel = "spring")
interface TelegramCopyTextButtonEntityMapper {
    fun toEntity(model: TelegramCopyTextButton): TelegramCopyTextButtonEntity
    fun toModel(entity: TelegramCopyTextButtonEntity): TelegramCopyTextButton
}
