package space.davids_digital.kiri

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import space.davids_digital.kiri.orm.service.UserSessionOrmService
import space.davids_digital.kiri.rest.auth.AuthenticationFilter
import space.davids_digital.kiri.rest.removeAuthCookies

@Configuration
class SecurityConfig {
    @Bean
    fun corsConfigurationSource(settings: Settings): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowedOrigins = listOf(settings.frontend.host)
        corsConfiguration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        corsConfiguration.allowedHeaders = listOf("*")
        corsConfiguration.allowCredentials = true
        corsConfiguration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(
        security: HttpSecurity,
        userSessionOrmService: UserSessionOrmService,
        corsConfigurationSource: CorsConfigurationSource,
        settings: Settings,
    ): SecurityFilterChain {
        security
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/auth/**", "/logout", "/ping").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                AuthenticationFilter(userSessionOrmService),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    handleAuthException(response, settings.frontend.cookiesDomain)
                }
                it.accessDeniedHandler { _, response, _ ->
                    handleAuthException(response, settings.frontend.cookiesDomain)
                }
            }
            .logout { it.disable() }
        return security.build()
    }

    private fun handleAuthException(response: HttpServletResponse, cookiesDomain: String) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.removeAuthCookies(cookiesDomain)
        response.contentType = "text/plain;charset=UTF-8"
    }
}
