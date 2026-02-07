package space.davids_digital.kiri.integration.google

import com.google.genai.Client
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
class GoogleGenAiClientHolder (
    private val settings: SettingOrmService,
) {
    object SettingKeys {
        const val API_KEY = "integration.google.genAi.apiKey"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clientRef = AtomicReference<Client?>()

    @PostConstruct
    private fun init() {
        scope.launch {
            settings.listen(SettingKeys.API_KEY).collectLatest {
                try {
                    onApiKeyChange(it)
                } catch (e: Exception) {
                    log.error("Failed to handle Google GenAI API Key change", e)
                }
            }
        }
    }

    private fun onApiKeyChange(newValue: String?) {
        log.info("Got new Google GenAI API key")
        val apiKey = newValue?.takeIf { it.isNotBlank() }
        if (apiKey == null) {
            log.warn("Google GenAI API key is empty, client will be disabled")
            clientRef.set(null)
            return
        }
        clientRef.set(buildClient(apiKey))
        log.info("Google GenAI client created")
    }

    private fun buildClient(apiKey: String): Client {
        return Client.builder().apiKey(apiKey).build()
    }

    fun requireClient() = clientRef.get() ?: error("Google GenAI client is not configured")

    fun getClient() = clientRef.get()
}