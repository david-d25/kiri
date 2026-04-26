package space.davids_digital.kiri.bot.telegram.command

import org.springframework.stereotype.Component
import space.davids_digital.kiri.orm.service.SettingOrmService

private const val OWNER_PLACEHOLDER = "{owner}"
private const val NOT_CONFIGURED_MESSAGE =
    "This bot's owner has not configured payment-related text yet. Please ask the bot administrator."

private val DEFAULT_TERMS = """
    Terms & Conditions

    This bot accepts voluntary donations in Telegram Stars (XTR). No goods or services are provided in
    exchange — donations support the operation and development of the bot.

    Refunds: You may request a refund within 21 days of payment. Use the /paysupport command or contact
    {owner}. Telegram may also offer a built-in refund option in the payment receipt.

    The bot is provided "as is" without warranties of availability, accuracy, or fitness for any
    purpose.

    For questions, complaints, or disputes, contact {owner}.

    By sending a payment you confirm that you have read and accepted these terms.
""".trimIndent()

private const val DEFAULT_SUPPORT = "For any questions about this bot, contact {owner}."

private val DEFAULT_PAY_SUPPORT = """
    For payment-related questions or refund requests, contact {owner}. Refunds are available within 21
    days of payment — please include the charge id from your Telegram payment receipt.
""".trimIndent()

@Component
class PaymentTextSettings(settings: SettingOrmService) {
    private val ownerUsername by settings.declareString("payments.ownerUsername", "")
    private val terms by settings.declareString("payments.termsText", DEFAULT_TERMS)
    private val support by settings.declareString("payments.supportText", DEFAULT_SUPPORT)
    private val paySupport by settings.declareString("payments.paySupportText", DEFAULT_PAY_SUPPORT)

    fun terms(): String = render(terms)
    fun support(): String = render(support)
    fun paySupport(): String = render(paySupport)

    private fun render(template: String): String {
        val username = ownerUsername.trim()
        if (username.isEmpty() && template.contains(OWNER_PLACEHOLDER)) {
            return NOT_CONFIGURED_MESSAGE
        }
        val handle = if (username.startsWith("@")) username else "@$username"
        return template.replace(OWNER_PLACEHOLDER, handle)
    }
}
