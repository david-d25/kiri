package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
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
    @Mappings(
        Mapping(source = "copyText", target = "copyTextButton")
    )
    fun toEntity(model: TelegramInlineKeyboardButton): TelegramInlineKeyboardButtonEntity

    @Mappings(
        Mapping(source = "copyTextButton", target = "copyText")
    )
    fun toModel(entity: TelegramInlineKeyboardButtonEntity): TelegramInlineKeyboardButton
}
