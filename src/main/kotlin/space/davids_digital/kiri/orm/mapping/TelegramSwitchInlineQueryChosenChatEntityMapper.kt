package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramSwitchInlineQueryChosenChat
import space.davids_digital.kiri.orm.entity.telegram.TelegramSwitchInlineQueryChosenChatEntity

@Mapper(componentModel = "spring")
interface TelegramSwitchInlineQueryChosenChatEntityMapper {
    fun toEntity(model: TelegramSwitchInlineQueryChosenChat): TelegramSwitchInlineQueryChosenChatEntity
    fun toModel(entity: TelegramSwitchInlineQueryChosenChatEntity): TelegramSwitchInlineQueryChosenChat
}
