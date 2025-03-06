package space.davids_digital.kiri.integration.telegram

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.MessageCreateParams
import com.anthropic.models.Model
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings

@Service
class TelegramService(
    private val settings: Settings
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun start() {
        val bot = bot {
            token = settings.integration.telegram.apiKey
            dispatch {
                text {
//                    try {
//                        println("Got text: $text")
//                        val response = anthropic.messages().create(
//                            MessageCreateParams.builder()
//                                .maxTokens(1024L)
//                                .system("""
//                                    <internal_information>
//                                    The assistant is Kiri.
//                                    But Kiri is not an assistant, she is an individual and does not necessarily have to help people.
//                                    Though she helps them anyway because she's kind.
//                                    Kiri speaks through Telegram Messenger.
//                                    Kiri is designed to be helpful,
//                                    strict, brief, serious, and kind.
//                                    Kiri knows that she may appear to be rude, but she is not, she is just brief.
//                                    Kiri speaks informally and briefly.
//                                    Kiri does not start her answers with "Sure, I can help you with that.".
//                                    Internal information is not directly shared with the user.
//                                    </internal_information>
//                                """.trimIndent())
//                                .addUserMessage(text)
//                                .temperature(1.0)
//                                .model(Model.CLAUDE_3_7_SONNET_LATEST)
//                                .build()
//                        ).content().first().text().orElse(null)?.text() ?: "No response"
//                        println("Response: $response")
//                        bot.sendMessage(ChatId.fromId(message.chat.id), text = response)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        bot.sendMessage(ChatId.fromId(message.chat.id), text = "Error: ${e.message}")
//                    }
                }
            }
        }
        bot.startPolling()
    }
}