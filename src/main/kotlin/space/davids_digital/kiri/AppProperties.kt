package space.davids_digital.kiri

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties (
    val version: String,
    val security: Security,
    val frontend: Frontend,
    val backend: Backend,
    val auth: Auth,
    val integration: Integration,
) {
    data class Security(val encryptionKeyBase64: String)

    data class Frontend(val host: String, val basePath: String, val cookiesDomain: String)

    data class Backend(val host: String, val basePath: String)

    data class Auth(val telegram: Telegram) {
        data class Telegram(val callbackUrl: String, val botUsername: String)
    }

    data class Integration(val telegram: Telegram, val anthropic: Anthropic, val openai: OpenAI, val google: Google) {
        data class Telegram(val botId: Long, val apiKey: String)
        data class Anthropic(val apiKey: String)
        data class OpenAI(val apiKey: String)
        data class Google(val genAi: GenAi) {
            data class GenAi(val apiKey: String)
        }
    }
}