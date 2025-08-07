package space.davids_digital.kiri.rest.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.AppProperties
import space.davids_digital.kiri.orm.service.UserSessionOrmService
import space.davids_digital.kiri.rest.CookieName
import space.davids_digital.kiri.rest.auth.UserAuthentication

@RestController
@RequestMapping("/logout")
class LogoutController(
    private val appProperties: AppProperties,
    private val userSessionOrmService: UserSessionOrmService,
) {
    @PostMapping
    fun logout(): ResponseEntity<Void> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated && authentication is UserAuthentication) {
            userSessionOrmService.delete(authentication.session.id)
        }
        SecurityContextHolder.clearContext()

        val userIdCookie = ResponseCookie.from(CookieName.USER_ID, "")
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .domain(appProperties.frontend.cookiesDomain)
            .maxAge(0)
            .build()
        val authCookie = ResponseCookie.from(CookieName.AUTH_TOKEN, "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .domain(appProperties.frontend.cookiesDomain)
            .maxAge(0)
            .build()

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, userIdCookie.toString())
            .header(HttpHeaders.SET_COOKIE, authCookie.toString())
            .build()
    }
}