package space.davids_digital.kiri

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class Settings (
    val version: String,
    val security: Security,
    val frontend: Frontend,
    val database: Database,
    val integration: Integration,
) {
    data class Security(val encryptionKeyBase64: String)
    data class Frontend(val host: String, val basePath: String, val cookiesDomain: String)
    data class Database(val postgres: Postgres) {
        data class Postgres(
            val host: String,
            val port: Int,
            val dbName: String,
            val username: String,
            val password: String,
        )
    }
    data class Integration(val telegram: Telegram, val anthropic: Anthropic, val openai: OpenAI, val google: Google) {
        data class Telegram(val apiKey: String)
        data class Anthropic(val apiKey: String)
        data class OpenAI(val apiKey: String)
        data class Google(val genAi: GenAi) {
            data class GenAi(val apiKey: String)
        }
    }
}