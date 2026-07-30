package space.davids_digital.kiri.rest.mapper.telegram

import org.mapstruct.AnnotateWith
import org.mapstruct.Mapper
import org.springframework.context.annotation.Primary
import space.davids_digital.kiri.model.TelegramBroadcast
import space.davids_digital.kiri.rest.dto.telegram.TelegramBroadcastDto

@Mapper
@AnnotateWith(Primary::class)
interface TelegramBroadcastDtoMapper {
    fun toDto(model: TelegramBroadcast?): TelegramBroadcastDto?
}
