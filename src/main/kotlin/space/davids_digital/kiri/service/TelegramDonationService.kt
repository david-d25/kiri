package space.davids_digital.kiri.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.integration.telegram.TelegramService.Companion.DONATION_CURRENCY
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramMessageOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramUserOrmService
import space.davids_digital.kiri.orm.specifications.telegram.TelegramMessageSpecifications
import space.davids_digital.kiri.rest.dto.telegram.TelegramDonationDto
import space.davids_digital.kiri.rest.dto.telegram.TelegramDonationsSummaryDto

@Service
class TelegramDonationService(
    private val messageOrm: TelegramMessageOrmService,
    private val chatOrm: TelegramChatOrmService,
    private val userOrm: TelegramUserOrmService,
    private val telegram: TelegramService,
) {
    @Transactional(readOnly = true)
    fun list(chatId: Long?, page: Int, size: Int): Page<TelegramDonationDto> {
        val spec = baseSpec(chatId)
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"))
        val messages = messageOrm.search(spec, pageable)
        val refunds = findRefundsFor(messages.content)
        val originalChatIds = messages.content.map { originalChatIdFor(it) }.distinct()
        val chats = chatOrm.findByIds(originalChatIds).associateBy { it.id }
        val users = userOrm.findAllById(messages.content.mapNotNull { it.fromId }.distinct()).associateBy { it.id }
        return messages.map { toDto(it, refunds[it.successfulPayment!!.telegramPaymentChargeId], chats, users) }
    }

    @Transactional(readOnly = true)
    fun summary(chatId: Long?): TelegramDonationsSummaryDto {
        val spec = baseSpec(chatId)
        // TODO: replace with SQL aggregate (sum/count) when donation volume grows.
        val all = messageOrm.search(spec, PageRequest.of(0, Int.MAX_VALUE)).content
        val refunds = findRefundsFor(all)
        val totalReceived = all.sumOf { it.successfulPayment!!.totalAmount.toLong() }
        val totalRefunded = all
            .filter { refunds.containsKey(it.successfulPayment!!.telegramPaymentChargeId) }
            .sumOf { it.successfulPayment!!.totalAmount.toLong() }
        return TelegramDonationsSummaryDto(
            totalReceived = totalReceived,
            totalRefunded = totalRefunded,
            net = totalReceived - totalRefunded,
            countTotal = all.size.toLong(),
            countRefunded = refunds.size.toLong(),
            currency = DONATION_CURRENCY,
        )
    }

    suspend fun refund(telegramPaymentChargeId: String): TelegramDonationDto {
        val donation = findDonationByChargeId(telegramPaymentChargeId)
            ?: throw IllegalArgumentException("Donation with charge id $telegramPaymentChargeId not found")
        val userId = donation.fromId
            ?: throw IllegalStateException("Donation $telegramPaymentChargeId has no fromId — cannot refund")
        telegram.refundStarPayment(userId, telegramPaymentChargeId)
        // After refund Telegram sends a service message; the refund record may not yet be in DB at return time.
        // Return current snapshot — UI can re-fetch list.
        return buildDtoForChargeId(donation)
    }

    @Transactional(readOnly = true)
    fun findDonationByChargeId(telegramPaymentChargeId: String): TelegramMessage? {
        val spec = TelegramMessageSpecifications.successfulPaymentChargeIdEquals(telegramPaymentChargeId)
        return messageOrm.search(spec, PageRequest.of(0, 1)).content.firstOrNull()
    }

    @Transactional(readOnly = true)
    fun buildDtoForChargeId(donation: TelegramMessage): TelegramDonationDto {
        val refunds = findRefundsFor(listOf(donation))
        val chats = chatOrm.findByIds(listOf(originalChatIdFor(donation))).associateBy { it.id }
        val users = donation.fromId?.let { userOrm.findAllById(listOf(it)).associateBy { u -> u.id } } ?: emptyMap()
        return toDto(
            donation,
            refunds[donation.successfulPayment!!.telegramPaymentChargeId],
            chats,
            users,
        )
    }

    private fun originalChatIdFor(donation: TelegramMessage): Long {
        val payload = donation.successfulPayment?.invoicePayload ?: return donation.chatId
        return TelegramService.extractDonationChatId(payload) ?: donation.chatId
    }

    private fun baseSpec(chatId: Long?) =
        TelegramMessageSpecifications.successfulPaymentNotNull()
            .and(TelegramMessageSpecifications.successfulPaymentCurrency(DONATION_CURRENCY))
            .let { if (chatId != null) it.and(TelegramMessageSpecifications.chatId(chatId)) else it }

    private fun findRefundsFor(donations: List<TelegramMessage>): Map<String, TelegramMessage> {
        val chargeIds = donations.mapNotNull { it.successfulPayment?.telegramPaymentChargeId }.distinct()
        if (chargeIds.isEmpty()) return emptyMap()
        val spec = TelegramMessageSpecifications.refundedPaymentChargeIdIn(chargeIds)
        val refundMessages = messageOrm.search(spec, PageRequest.of(0, chargeIds.size.coerceAtLeast(1))).content
        return refundMessages.associateBy { it.refundedPayment!!.telegramPaymentChargeId }
    }

    private fun toDto(
        donation: TelegramMessage,
        refund: TelegramMessage?,
        chats: Map<Long, TelegramChat>,
        users: Map<Long, TelegramUser>,
    ): TelegramDonationDto {
        val payment = donation.successfulPayment!!
        val originalChatId = originalChatIdFor(donation)
        val chat = chats[originalChatId]
        val user = donation.fromId?.let { users[it] }
        return TelegramDonationDto(
            telegramPaymentChargeId = payment.telegramPaymentChargeId,
            providerPaymentChargeId = payment.providerPaymentChargeId,
            chatId = originalChatId,
            chatTitle = chat?.title,
            chatUsername = chat?.username,
            userId = donation.fromId,
            userUsername = user?.username,
            userFirstName = user?.firstName,
            currency = payment.currency,
            totalAmount = payment.totalAmount,
            invoicePayload = payment.invoicePayload,
            paidAt = donation.date.toOffsetDateTime(),
            refunded = refund != null,
            refundedAt = refund?.date?.toOffsetDateTime(),
            messageId = donation.messageId,
        )
    }
}
