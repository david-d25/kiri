package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramSwitchInlineQueryChosenChat
import space.davids_digital.kiri.orm.entity.telegram.TelegramSwitchInlineQueryChosenChatEntity

@Mapper(componentModel = "spring")
interface TelegramSwitchInlineQueryChosenChatEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramSwitchInlineQueryChosenChat?): TelegramSwitchInlineQueryChosenChatEntity?
    fun toModel(entity: TelegramSwitchInlineQueryChosenChatEntity?): TelegramSwitchInlineQueryChosenChat?
}
