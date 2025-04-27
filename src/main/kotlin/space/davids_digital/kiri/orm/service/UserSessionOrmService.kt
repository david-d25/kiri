package space.davids_digital.kiri.orm.service

import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.UserSession
import space.davids_digital.kiri.orm.entity.UserSessionEntity
import space.davids_digital.kiri.orm.repository.UserSessionRepository
import space.davids_digital.kiri.service.CryptographyService
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

@Service
class UserSessionOrmService(
    private val userSessionRepository: UserSessionRepository,
    private val cryptographyService: CryptographyService
) {
    @Transactional
    fun delete(sessionId: UUID) {
        userSessionRepository.deleteById(sessionId)
    }

    @Transactional
    fun save(session: UserSession): UserSession {
        val entity = UserSessionEntity()
        entity.userId = session.userId
        try {
            val tokenBytes = session.token.decodeBase64Bytes()
            entity.tokenEncrypted = cryptographyService.encrypt(tokenBytes)
        } catch (e: Exception) {
            throw RuntimeException("Encryption error", e)
        }
        entity.validUntil = session.validUntil?.toOffsetDateTime()
        entity.firstName = session.firstName
        entity.lastName = session.lastName
        entity.username = session.username
        entity.photoUrl = session.photoUrl
        entity.authDate = session.authDate.toOffsetDateTime()
        entity.hash = session.hash
        return toModel(userSessionRepository.save(entity))
    }

    @Transactional(readOnly = true)
    fun getUnexpiredUserSessionsByUserId(userId: Long): Collection<UserSession> {
        return userSessionRepository.findAllByUserIdAndValidUntilAfterOrNull(userId, OffsetDateTime.now())
            .map { toModel(it) }
            .toList()
    }

    @Transactional
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun cleanUpOldTokens() {
        userSessionRepository.deleteByValidUntilBefore(OffsetDateTime.now())
    }

    private fun toModel(entity: UserSessionEntity): UserSession {
        return try {
            UserSession(
                entity.id,
                entity.userId,
                cryptographyService.decrypt(entity.tokenEncrypted).encodeBase64(),
                entity.validUntil?.toZonedDateTime(),
                entity.firstName,
                entity.lastName,
                entity.username,
                entity.photoUrl,
                entity.authDate.toZonedDateTime(),
                entity.hash
            )
        } catch (_: IllegalBlockSizeException) {
            throw UserSessionDecryptException("Couldn't decrypt access tokens")
        } catch (_: BadPaddingException) {
            throw UserSessionDecryptException("Couldn't decrypt access tokens")
        } catch (e: Exception) {
            throw RuntimeException("Decryption error", e)
        }
    }

    /**
     * Thrown when failed to decrypt user session fields
     */
    class UserSessionDecryptException : Exception {
        constructor(message: String?) : super(message)
        constructor() : super()
    }
}
