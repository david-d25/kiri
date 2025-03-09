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
        // Temporary draft just to test the prompt
        val response = anthropic.messages().create(
            MessageCreateParams.builder()
                .maxTokens(1024L)
                .system(this::class.java.getResource("/prompts/main.txt")?.readText() ?: "")
                .addUserMessage(input)
                .temperature(1.0)
                .model(Model.CLAUDE_3_7_SONNET_LATEST)
                .build()
        )
        return response.toString()
    }
}