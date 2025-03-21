package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.ValueMapping
import space.davids_digital.kiri.model.telegram.TelegramSticker.Type

@Mapper(componentModel = "spring")
interface TelegramStickerEntityTypeMapper {
    fun map(type: Type): String

    @ValueMapping(source = "<ANY_UNMAPPED>", target = "UNKNOWN")
    fun map(type: String): Type
}