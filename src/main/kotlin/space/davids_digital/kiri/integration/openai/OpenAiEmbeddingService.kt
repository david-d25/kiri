package space.davids_digital.kiri.integration.openai

import com.aallam.openai.api.embedding.EmbeddingRequest
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.RetryStrategy
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings
import kotlin.collections.toFloatArray
import kotlin.time.Duration.Companion.minutes

@Service
class OpenAiEmbeddingService(settings: Settings) {
    private val openai = OpenAI(
        token = settings.integration.openai.apiKey,
        logging = LoggingConfig(logLevel = LogLevel.None),
        timeout = Timeout(5.minutes),
        retry = RetryStrategy(maxRetries = 5)
    )

    suspend fun createEmbeddings(model: String, texts: List<String>, dimensions: Int): List<FloatArray> {
        if (texts.isEmpty()) {
            return emptyList()
        }
        val request = EmbeddingRequest(
            model = ModelId(model),
            input = texts,
            dimensions = dimensions
        )
        val response = openai.embeddings(request)
        return response.embeddings.map { it.embedding.map(Double::toFloat).toFloatArray() }
    }
}