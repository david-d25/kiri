package space.davids_digital.kiri.rest.mapper.telegram

import org.mapstruct.AnnotateWith
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.rest.dto.TelegramChatMetadataDto
import space.davids_digital.kiri.rest.dto.telegram.TelegramChatDto
import space.davids_digital.kiri.service.TelegramChatService

@Mapper
@AnnotateWith(Primary::class)
abstract class TelegramChatDtoMapper {
    @Autowired
    private lateinit var telegramChatService: TelegramChatService

    @Mapping(target = "smallPhotoUrl", source = ".", qualifiedByName = ["smallPhotoUrl"])
    @Mapping(target = "bigPhotoUrl",   source = ".", qualifiedByName = ["bigPhotoUrl"])
    abstract fun toDto(model: TelegramChat?): TelegramChatDto?

    abstract fun toModel(dto: TelegramChatDto.Type?): TelegramChat.Type?

    abstract fun toModel(dto: TelegramChatMetadataDto.NotificationMode?): TelegramChat.NotificationMode?

    @Suppress("unused") // Used here
    @Named("smallPhotoUrl")
    fun smallPhotoUrl(chat: TelegramChat?): String? =
        chat?.photo?.let {
            telegramChatService.createChatPhotoUrl(it.smallFileId)
        }

    @Suppress("unused") // Used here
    @Named("bigPhotoUrl")
    fun bigPhotoUrl(chat: TelegramChat?): String? =
        chat?.photo?.let {
            telegramChatService.createChatPhotoUrl(it.bigFileId)
        }
}