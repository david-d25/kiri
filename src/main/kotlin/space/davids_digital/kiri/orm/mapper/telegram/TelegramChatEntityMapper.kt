package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatEntity
import space.davids_digital.kiri.orm.mapper.TelegramChatMetadataEntityMapper

@Mapper(
    uses = [
        TelegramChatPhotoEntityMapper::class,
        TelegramMessageEntityMapper::class,
        TelegramChatPermissionsEntityMapper::class,
        TelegramChatLocationEntityMapper::class,
        TelegramChatMetadataEntityMapper::class
    ]
)
interface TelegramChatEntityMapper {
    fun toEntity(model: TelegramChat?): TelegramChatEntity?
    fun toModel(source: TelegramChatEntity?, metadata: TelegramChat.Metadata?): TelegramChat?
    fun toEntity(model: TelegramChat.Type?): TelegramChatEntity.Type?
    fun toModel(entity: TelegramChatEntity.Type?): TelegramChat.Type?
}
