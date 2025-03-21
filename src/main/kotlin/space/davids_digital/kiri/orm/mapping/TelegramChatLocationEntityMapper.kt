package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramChatLocation
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatLocationEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramLocationEntityMapper::class]
)
interface TelegramChatLocationEntityMapper {
    fun toEntity(model: TelegramChatLocation): TelegramChatLocationEntity
    fun toModel(entity: TelegramChatLocationEntity): TelegramChatLocation
}
