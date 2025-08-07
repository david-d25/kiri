package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatEntity

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramChatPhotoEntityMapper::class,
        TelegramMessageEntityMapper::class,
        TelegramChatPermissionsEntityMapper::class,
        TelegramChatLocationEntityMapper::class
    ]
)
interface TelegramChatEntityMapper {
    fun toEntity(model: TelegramChat?): TelegramChatEntity?
    fun toModel(entity: TelegramChatEntity?): TelegramChat?
    fun toEntity(model: TelegramChat.Type?): TelegramChatEntity.Type?
    fun toModel(entity: TelegramChatEntity.Type?): TelegramChat.Type?
}
