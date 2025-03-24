package space.davids_digital.kiri.orm.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.UserSession
import space.davids_digital.kiri.orm.entity.UserSessionEntity
import space.davids_digital.kiri.orm.repository.UserSessionRepository
import space.davids_digital.kiri.service.CryptographyService
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
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
    fun deleteUserSession(sessionId: UUID) {
        userSessionRepository.deleteById(sessionId)
    }

    @Transactional
    fun createUserSession(userId: Long, sessionToken: String, validUntil: ZonedDateTime): UserSession {
        val entity = UserSessionEntity()
        entity.userId = userId
        try {
            entity.sessionTokenEncrypted = cryptographyService.encrypt(sessionToken.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            throw RuntimeException("Encryption error", e)
        }
        entity.validUntil = validUntil
        return toModel(userSessionRepository.save(entity))
    }

    @Transactional(readOnly = true)
    fun getUnexpiredUserSessionsByUserId(userId: Long): Collection<UserSession> {
        return userSessionRepository.findAllByUserIdAndValidUntilAfter(userId, ZonedDateTime.now())
            .map { toModel(it) }
            .toList()
    }

    @Transactional
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun cleanUpOldTokens() {
        userSessionRepository.deleteByValidUntilBefore(ZonedDateTime.now())
    }

    private fun toModel(entity: UserSessionEntity): UserSession {
        return try {
            UserSession(
                entity.id,
                entity.userId,
                String(cryptographyService.decrypt(entity.sessionTokenEncrypted), StandardCharsets.UTF_8),
                entity.validUntil
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
