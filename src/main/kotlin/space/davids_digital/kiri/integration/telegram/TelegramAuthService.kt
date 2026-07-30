package space.davids_digital.kiri.integration.telegram

import org.springframework.stereotype.Service
import space.davids_digital.kiri.AppProperties
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class TelegramAuthService(
    private val appProperties: AppProperties
) {
    companion object {
        /**
         * Telegram redirects to the callback immediately after login, so anything older than this is a
         * replay of a captured callback URL rather than a fresh sign-in. The window also absorbs clock skew.
         */
        private val MAX_AUTH_AGE: Duration = Duration.ofMinutes(5)
    }

    fun validateAuth(
        userId: Long,
        firstName: String?,
        lastName: String?,
        username: String?,
        photoUrl: String?,
        authDate: Long,
        hash: String,
    ): Boolean {
        val age = Duration.between(Instant.ofEpochSecond(authDate), Instant.now()).abs()
        if (age > MAX_AUTH_AGE) {
            return false
        }
        val computedHash = computeHash(userId, firstName, lastName, username, photoUrl, authDate)
        return MessageDigest.isEqual(
            computedHash.lowercase().toByteArray(Charsets.US_ASCII),
            hash.lowercase().toByteArray(Charsets.US_ASCII)
        )
    }

    fun computeHash(
        userId: Long,
        firstName: String?,
        lastName: String?,
        username: String?,
        photoUrl: String?,
        authDate: Long
    ): String {
        val botToken = appProperties.integration.telegram.apiKey
        val sha256 = MessageDigest.getInstance("SHA-256")
        val secretKey = sha256.digest(botToken.toByteArray())

        val params = mutableMapOf<String, String>()
        params["auth_date"] = authDate.toString()
        params["id"] = userId.toString()
        firstName?.let { params["first_name"] = it }
        lastName?.let { params["last_name"] = it }
        username?.let { params["username"] = it }
        photoUrl?.let { params["photo_url"] = it }

        val dataCheckString = params.entries
            .sortedBy { it.key }
            .joinToString("\n") { "${it.key}=${it.value}" }

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey, "HmacSHA256"))
        val computed = mac.doFinal(dataCheckString.toByteArray())
        return computed.joinToString("") { String.format("%02x", it) }
    }
}