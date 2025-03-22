package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramInlineKeyboardButton
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramWebAppInfoEntityMapper::class,
        TelegramLoginUrlEntityMapper::class,
        TelegramSwitchInlineQueryChosenChatEntityMapper::class,
        TelegramCopyTextButtonEntityMapper::class,
    ]
)
interface TelegramInlineKeyboardButtonEntityMapper {
    @Mapping(source = "copyText", target = "copyTextButton")
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramInlineKeyboardButton?): TelegramInlineKeyboardButtonEntity?

    @Mapping(source = "copyTextButton", target = "copyText")
    @Mapping(target = "callbackGame", ignore = true)
    fun toModel(entity: TelegramInlineKeyboardButtonEntity?): TelegramInlineKeyboardButton?
}
