package space.davids_digital.kiri

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import space.davids_digital.kiri.model.User
import space.davids_digital.kiri.orm.service.UserOrmService
import space.davids_digital.kiri.orm.service.UserSessionOrmService
import space.davids_digital.kiri.rest.auth.AuthenticationFilter
import space.davids_digital.kiri.rest.removeAuthCookies

@Configuration
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    fun roleHierarchy(): RoleHierarchy {
        return RoleHierarchyImpl.withDefaultRolePrefix()
            .role(User.Role.OWNER.name).implies(User.Role.ADMIN.name)
            .build()
    }

    @Bean
    fun templateExpressionDefaults(): AnnotationTemplateExpressionDefaults {
        return AnnotationTemplateExpressionDefaults()
    }

    @Bean
    fun methodSecurityExpressionHandler(roleHierarchy: RoleHierarchy): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setRoleHierarchy(roleHierarchy)
        return expressionHandler
    }

    @Bean
    fun corsConfigurationSource(appProperties: AppProperties): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowedOrigins = listOf(appProperties.frontend.host)
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
        userOrmService: UserOrmService,
        corsConfigurationSource: CorsConfigurationSource,
        appProperties: AppProperties,
    ): SecurityFilterChain {
        security
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() } // TODO prepare frontend and enable
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/auth/**", "/bootstrap", "/logout", "/ping").permitAll()
                    .anyRequest().hasRole(User.Role.ADMIN.name)
            }
            .addFilterBefore(
                AuthenticationFilter(userSessionOrmService, userOrmService),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    handleAuthException(response, appProperties.frontend.cookiesDomain)
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
