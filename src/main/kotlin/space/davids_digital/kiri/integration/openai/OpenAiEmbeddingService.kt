package space.davids_digital.kiri.integration.openai

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.embeddings.EmbeddingCreateParams
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.model.Setting
import space.davids_digital.kiri.orm.service.SettingOrmService
import java.util.concurrent.atomic.AtomicReference

@Service
class OpenAiEmbeddingService(private val settings: SettingOrmService) {
    object SettingKeys {
        const val API_KEY = "integration.openai.apiKey"
    }

    class ServiceDisabledException : RuntimeException()
    private val log = LoggerFactory.getLogger(javaClass)

    private val clientRef = AtomicReference<OpenAIClient?>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @PostConstruct
    private fun init() {
        scope.launch {
            settings.listen(SettingKeys.API_KEY).collectLatest {
                try {
                    onApiKeyChange(it)
                } catch (e: Exception) {
                    log.error("Failed to handle OpenAI API Key change", e)
                }
            }
        }
    }

    private fun onApiKeyChange(setting: Setting) {
        log.info("Got new OpenAI API key")
        val apiKey = setting.value?.takeIf { it.isNotBlank() }
        if (apiKey == null) {
            log.warn("OpenAI API key is empty, client will be disabled")
            clientRef.set(null)
            return
        }
        clientRef.set(buildClient(apiKey))
        log.info("OpenAI client created")
    }

    private fun buildClient(apiKey: String): OpenAIClient {
        return OpenAIOkHttpClient.builder().apiKey(apiKey).build()
    }

    private fun requireClient() = clientRef.get() ?: throw ServiceDisabledException()

    suspend fun createEmbeddings(model: String, texts: List<String>, dimensions: Int): List<FloatArray> {
        if (texts.isEmpty()) {
            return emptyList()
        }
        val client = requireClient()
        val response = client.embeddings().create(
            EmbeddingCreateParams.builder()
                .model(model)
                .inputOfArrayOfStrings(texts)
                .dimensions(dimensions.toLong())
                .build()
        )
        return response.data().map { it.embedding().toFloatArray() }
    }
}