package space.davids_digital.kiri.rest.controller.telegram

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.rest.dto.telegram.TelegramBroadcastDto
import space.davids_digital.kiri.rest.dto.telegram.TelegramBroadcastRequest
import space.davids_digital.kiri.rest.dto.telegram.TelegramBroadcastStatusDto
import space.davids_digital.kiri.rest.mapper.telegram.TelegramBroadcastDtoMapper
import space.davids_digital.kiri.service.TelegramBroadcastService

@RestController
@RequestMapping("/telegram/broadcast")
class TelegramBroadcastController(
    private val mapper: TelegramBroadcastDtoMapper,
    private val broadcastService: TelegramBroadcastService,
) {
    @GetMapping("status")
    fun status(): TelegramBroadcastStatusDto {
        return TelegramBroadcastStatusDto(
            recipients = broadcastService.countRecipients(),
            latest = broadcastService.findLatest()?.let(mapper::toDto),
        )
    }

    @PostMapping
    fun start(@RequestBody request: TelegramBroadcastRequest): TelegramBroadcastDto {
        return mapper.toDto(broadcastService.start(request.text, request.silent))!!
    }
}
