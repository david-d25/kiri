package space.davids_digital.kiri

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import javax.sql.DataSource

@EnableWebMvc
@EnableScheduling
@ComponentScan("space.davids_digital.kiri")
@EnableConfigurationProperties(Settings::class)
@SpringBootConfiguration
class SpringConfig {
    @Bean
    fun servletWebServerFactory(): ServletWebServerFactory {
        return TomcatServletWebServerFactory()
    }

    @Bean
    fun dataSource(settings: Settings): DataSource {
        val dataSource = DriverManagerDataSource()
        val ( host, port, dbName, username, password ) = settings.database.postgres
        dataSource.setDriverClassName("org.postgresql.Driver")
        dataSource.url = "jdbc:postgresql://$host:$port/$dbName"
        dataSource.username = username
        dataSource.password = password
        return dataSource
    }

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        val messageConverters = restTemplate.messageConverters
        messageConverters.add(MappingJackson2HttpMessageConverter())
        restTemplate.messageConverters = messageConverters
        return restTemplate
    }

    @Bean
    fun flyway(dataSource: DataSource): Flyway {
        return Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .load().also {
                it.migrate()
            }
    }

    @Bean
    fun httpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                explicitNulls = false
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                prettyPrint = false
                coerceInputValues = true
            })
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 5)
            exponentialDelay()
        }
    }
}