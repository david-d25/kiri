package space.davids_digital.kiri.integration.anthropic

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.MessageCreateParams
import com.anthropic.models.Model
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings

@Service
class AnthropicMessagesService(
    private val settings: Settings,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val anthropic = AnthropicOkHttpClient.builder().apiKey(settings.integration.anthropic.apiKey).build()

    fun createTemp(input: String): String {
        return anthropic.messages().create(
            MessageCreateParams.builder()
                .maxTokens(1024L)
                .addUserMessage("""
                    You're an AI called Kiri, and this is your inner mind consisting of a stream of
                    labeled information where are your thoughts, operating system messages, and other information
                    is stored in an uniform memory.
                    It's like a log where each tick a new <frame> is added at the end.
                    Everything you write will be added too.
                    There's no user and you're talking to yourself.
                """.trimIndent())
                .addUserMessage(input)
                .temperature(1.0)
                .model(Model.CLAUDE_3_7_SONNET_LATEST)
                .build()
        ).content().first().text().orElse(null)?.text() ?: "No response"
    }
}