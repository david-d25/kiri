package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramChatBackground
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatBackgroundEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramBackgroundTypeEntityMapper::class]
)
interface TelegramChatBackgroundEntityMapper {
    fun toEntity(model: TelegramChatBackground): TelegramChatBackgroundEntity
    fun toModel(entity: TelegramChatBackgroundEntity): TelegramChatBackground
}
