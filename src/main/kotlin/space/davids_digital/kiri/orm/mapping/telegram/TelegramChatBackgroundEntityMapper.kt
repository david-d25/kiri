package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramChatBackground
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatBackgroundEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramBackgroundTypeEntityMapper::class]
)
interface TelegramChatBackgroundEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramChatBackground?): TelegramChatBackgroundEntity?

    @Mapping(target = "copy", ignore = true)
    fun toModel(entity: TelegramChatBackgroundEntity?): TelegramChatBackground?
}
