package space.davids_digital.kiri.rest.controller.telegram

import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.rest.dto.telegram.TelegramDonationDto
import space.davids_digital.kiri.rest.dto.telegram.TelegramDonationsSummaryDto
import space.davids_digital.kiri.service.TelegramDonationService

@RestController
@RequestMapping("/telegram/donations")
class TelegramDonationsController(
    private val donations: TelegramDonationService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) chatId: Long?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): Page<TelegramDonationDto> {
        return donations.list(chatId, page, size)
    }

    @GetMapping("summary")
    fun summary(
        @RequestParam(required = false) chatId: Long?,
    ): TelegramDonationsSummaryDto {
        return donations.summary(chatId)
    }

    @PostMapping("{telegramPaymentChargeId}/refund")
    suspend fun refund(
        @PathVariable telegramPaymentChargeId: String,
    ): TelegramDonationDto {
        return donations.refund(telegramPaymentChargeId)
    }
}
