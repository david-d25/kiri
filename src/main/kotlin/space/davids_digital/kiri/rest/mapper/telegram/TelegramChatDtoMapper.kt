package space.davids_digital.kiri.rest.mapper.telegram

import org.mapstruct.AnnotateWith
import org.mapstruct.Mapper
import org.springframework.context.annotation.Primary
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.rest.dto.telegram.TelegramChatDto

@Mapper
@AnnotateWith(Primary::class)
interface TelegramChatDtoMapper {
    fun toDto(model: TelegramChat?): TelegramChatDto?
    fun toModel(dto: TelegramChatDto.Type?): TelegramChat.Type?
}