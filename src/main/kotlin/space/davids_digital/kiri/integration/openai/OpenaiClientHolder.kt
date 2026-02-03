package space.davids_digital.kiri.integration.openai

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import space.davids_digital.kiri.orm.service.SettingOrmService
import java.util.concurrent.atomic.AtomicReference

@Component
class OpenaiClientHolder (
    private val settings: SettingOrmService,
) {
    object SettingKeys {
        const val API_KEY = "integration.openai.apiKey"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clientRef = AtomicReference<OpenAIClient?>()

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

    private fun onApiKeyChange(newValue: String?) {
        log.info("Got new OpenAI API key")
        val apiKey = newValue?.takeIf { it.isNotBlank() }
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

    fun requireClient() = clientRef.get() ?: error("OpenAI client is not configured")

    fun getClient() = clientRef.get()
}