package space.davids_digital.kiri.rest.controller

import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.integration.telegram.TelegramAuthService

@Profile("dev")
@RestController
@RequestMapping("/auth/telegram")
class TelegramAuthDevController(private val telegramAuthService: TelegramAuthService) {
    @GetMapping("/dev/hash")
    fun devHash(
        userId: Long,
        firstName: String,
        lastName: String,
        username: String,
        photoUrl: String,
        authDate: Long
    ): ResponseEntity<String> {
        val hash = telegramAuthService.computeHash(
            userId,
            firstName,
            lastName,
            username,
            photoUrl,
            authDate
        )
        return ResponseEntity.ok(hash)
    }
}