package space.davids_digital.kiri.rest.dto.telegram

import java.time.OffsetDateTime

data class TelegramDonationDto (
    val telegramPaymentChargeId: String,
    val providerPaymentChargeId: String,
    val chatId: Long,
    val chatTitle: String?,
    val chatUsername: String?,
    val userId: Long?,
    val userUsername: String?,
    val userFirstName: String?,
    val currency: String,
    val totalAmount: Int,
    val invoicePayload: String,
    val paidAt: OffsetDateTime,
    val refunded: Boolean,
    val refundedAt: OffsetDateTime?,
    val messageId: Int,
)

data class TelegramDonationsSummaryDto (
    val totalReceived: Long,
    val totalRefunded: Long,
    val net: Long,
    val countTotal: Long,
    val countRefunded: Long,
    val currency: String,
)
