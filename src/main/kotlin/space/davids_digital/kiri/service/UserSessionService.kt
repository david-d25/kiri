package space.davids_digital.kiri.service

import io.ktor.util.encodeBase64
import org.springframework.stereotype.Service
import space.davids_digital.kiri.model.UserSession
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.service.UserSessionOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramUserOrmService
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.util.UUID

@Service
class UserSessionService(
    private val orm: UserSessionOrmService,
    private val telegramUserOrm: TelegramUserOrmService,
    private val telegramUserMetadataService: TelegramUserMetadataService
) {
    fun createSession(
        userId: Long,
        firstName: String,
        hash: String,
        authDate: ZonedDateTime = ZonedDateTime.now(),
        lastName: String? = null,
        username: String? = null,
        photoUrl: String? = null,
    ): UserSession {
        val session = UserSession(
            id = UUID.randomUUID(),
            userId = userId,
            token = createRandomSessionToken(),
            validUntil = null,
            firstName = firstName,
            lastName = lastName,
            username = username,
            photoUrl = photoUrl,
            authDate = authDate,
            hash = hash,
        )
        ensureUserExists(userId, firstName, lastName, username)
        return orm.save(session)
    }

    private fun ensureUserExists(userId: Long, firstName: String, lastName: String?, username: String?) {
        val savedUser = telegramUserOrm.findById(userId) ?: TelegramUser(
            id = userId,
            isBot = false,
            firstName = firstName,
            lastName = lastName,
            username = username,
            metadata = telegramUserMetadataService.getOrCreateDefault(userId)
        )
        telegramUserOrm.save(savedUser)
    }

    private fun createRandomSessionToken(): String {
        val randomBytes = ByteArray(32)
        val random = SecureRandom()
        random.nextBytes(randomBytes)
        return randomBytes.encodeBase64()
    }
}