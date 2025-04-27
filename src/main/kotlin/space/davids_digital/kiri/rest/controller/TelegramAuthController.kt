package space.davids_digital.kiri.rest.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.integration.telegram.TelegramAuthService
import space.davids_digital.kiri.rest.CookieName
import space.davids_digital.kiri.service.UserSessionService
import java.time.ZonedDateTime

/**
 * Handles Telegram OAuth login callback.
 */
@RestController
@RequestMapping("/auth/telegram")
class TelegramAuthController(
    private val settings: Settings,
    private val userSessionService: UserSessionService,
    private val telegramAuthService: TelegramAuthService
) {
    @GetMapping("/callback")
    fun callback(
        @RequestParam("id")         id: Long,
        @RequestParam("first_name") firstName: String,
        @RequestParam("last_name")  lastName: String,
        @RequestParam("username")   username: String,
        @RequestParam("photo_url")  photoUrl: String,
        @RequestParam("auth_date")  authDateEpochSeconds: Long,
        @RequestParam("hash")       hash: String
    ): ResponseEntity<Void> {
        val authDate = ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochSecond(authDateEpochSeconds),
            java.time.ZoneOffset.UTC
        )
        val authIsValid = telegramAuthService.validateAuth(
            id,
            firstName,
            lastName,
            username,
            photoUrl,
            authDateEpochSeconds,
            hash
        )
        if (!authIsValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val session = userSessionService.createSession(id, firstName, hash, authDate, lastName, username, photoUrl)

        val userIdCookie = ResponseCookie.from(CookieName.USER_ID, session.userId.toString())
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .domain(settings.frontend.cookiesDomain)
        val authCookie = ResponseCookie.from(CookieName.AUTH_TOKEN, session.token)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .domain(settings.frontend.cookiesDomain)

        if (session.validUntil != null) {
            val maxAge = session.validUntil.minusSeconds(ZonedDateTime.now().toEpochSecond()).toEpochSecond()
            userIdCookie.maxAge(maxAge)
            authCookie.maxAge(maxAge)
        }

        val redirectTo = settings.frontend.host.removeSuffix("/") + "/" + settings.frontend.basePath.removePrefix("/")

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.SET_COOKIE, userIdCookie.build().toString())
            .header(HttpHeaders.SET_COOKIE, authCookie.build().toString())
            .header(HttpHeaders.LOCATION, redirectTo)
            .build()
    }
}