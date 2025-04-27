package space.davids_digital.kiri.integration.telegram

import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class TelegramAuthService(
    private val settings: Settings
) {
    fun validateAuth(
        userId: Long,
        firstName: String,
        lastName: String,
        username: String,
        photoUrl: String,
        authDate: Long,
        hash: String,
    ): Boolean {
        val computedHash = computeHash(userId, firstName, lastName, username, photoUrl, authDate)
        return computedHash.lowercase() == hash.lowercase()
    }

    fun computeHash(
        userId: Long,
        firstName: String,
        lastName: String,
        username: String,
        photoUrl: String,
        authDate: Long
    ): String {
        val botToken = settings.integration.telegram.apiKey
        val sha256 = MessageDigest.getInstance("SHA-256")
        val secretKey = sha256.digest(botToken.toByteArray())
        val dataCheckString = StringBuilder()
            .append("auth_date=$authDate")
            .append("\nfirst_name=$firstName")
            .append("\nlast_name=$lastName")
            .append("\nphoto_url=$photoUrl")
            .append("\nusername=$username")
            .append("\nuser_id=$userId")
            .toString()
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey, "HmacSHA256"))
        val computed = mac.doFinal(dataCheckString.toByteArray())
        return computed.joinToString("") { String.format("%02x", it) }
    }
}