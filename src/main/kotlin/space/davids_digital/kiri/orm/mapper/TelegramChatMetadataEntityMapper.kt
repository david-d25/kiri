package space.davids_digital.kiri.orm.mapper

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.orm.entity.TelegramChatMetadataEntity

@Mapper(uses = [DateTimeMapper::class])
abstract class TelegramChatMetadataEntityMapper {
    abstract fun toModel(entity: TelegramChatMetadataEntity?): TelegramChat.Metadata?
    abstract fun toModel(entity: TelegramChatMetadataEntity.NotificationMode?): TelegramChat.NotificationMode

    abstract fun toEntity(model: TelegramChat.Metadata?, chatId: Long?): TelegramChatMetadataEntity?
    abstract fun toModel(entity: TelegramChat.NotificationMode?): TelegramChatMetadataEntity.NotificationMode?
}