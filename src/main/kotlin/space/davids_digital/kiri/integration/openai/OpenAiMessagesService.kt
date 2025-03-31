package space.davids_digital.kiri.integration.openai

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.RetryStrategy
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.llm.LlmMessageRequest
import space.davids_digital.kiri.llm.LlmMessageResponse
import space.davids_digital.kiri.service.LlmService
import kotlin.time.Duration.Companion.minutes

@Service
class OpenAiMessagesService(settings: Settings) : LlmService<ModelId> {
    private val openai = OpenAI(
        token = settings.integration.openai.apiKey,
        logging = LoggingConfig(logLevel = LogLevel.None),
        timeout = Timeout(5.minutes),
        retry = RetryStrategy(maxRetries = 5)
    )

    override suspend fun request(request: LlmMessageRequest): LlmMessageResponse {
        return TODO()
    }
}