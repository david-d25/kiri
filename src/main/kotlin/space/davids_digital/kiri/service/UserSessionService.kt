package space.davids_digital.kiri.service

import io.ktor.util.encodeBase64
import org.springframework.stereotype.Service
import space.davids_digital.kiri.model.UserSession
import space.davids_digital.kiri.orm.service.UserSessionOrmService
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.util.UUID

@Service
class UserSessionService(private val orm: UserSessionOrmService) {
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
        return orm.save(session)
    }

    private fun createRandomSessionToken(): String {
        val randomBytes = ByteArray(32)
        val random = SecureRandom()
        random.nextBytes(randomBytes)
        return randomBytes.encodeBase64()
    }
}