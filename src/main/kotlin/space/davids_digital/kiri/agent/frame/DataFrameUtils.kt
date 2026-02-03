package space.davids_digital.kiri.agent.frame

import space.davids_digital.kiri.llm.ChatCompletionImageType
import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DataFrameUtils {
    const val PRETTY_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss 'UTC'"
    private val UTC_FORMATTER = DateTimeFormatter.ofPattern(PRETTY_DATE_TIME_PATTERN)

    fun StaticDataFrame.Builder.addCreatedAtNow() {
        attributes["created-at"] = ZonedDateTime.now(ZoneOffset.UTC).asPrettyString()
    }

    fun ZonedDateTime.asPrettyString(): String = withZoneSameInstant(ZoneOffset.UTC).format(UTC_FORMATTER)
    fun String.fromPrettyStringToZonedDateTime(): ZonedDateTime =
        ZonedDateTime.parse(this, UTC_FORMATTER).withZoneSameInstant(ZoneOffset.UTC)

    fun ByteArray.getImageType(): ChatCompletionImageType? {
        if (this.size < 12) return null

        return when {
            this[0] == 0xFF.toByte() && this[1] == 0xD8.toByte() -> ChatCompletionImageType.JPEG
            this[0] == 0x89.toByte() && this[1] == 0x50.toByte() -> ChatCompletionImageType.PNG
            this[0] == 0x47.toByte() && this[1] == 0x49.toByte() -> ChatCompletionImageType.GIF
            this[0] == 0x52.toByte() && this[1] == 0x49.toByte() && // 'RIFF'
                    this[2] == 0x46.toByte() && this[3] == 0x46.toByte() &&
                    this[8] == 0x57.toByte() && this[9] == 0x45.toByte() &&
                    this[10] == 0x42.toByte() && this[11] == 0x50.toByte() -> ChatCompletionImageType.WEBP
            else -> null
        }
    }
}
