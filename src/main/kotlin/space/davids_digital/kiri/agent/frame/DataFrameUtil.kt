package space.davids_digital.kiri.agent.frame

import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val UTC_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'")

fun StaticDataFrame.Builder.addCreatedAtNow() {
    attributes["created-at"] = ZonedDateTime.now(ZoneOffset.UTC).asPrettyString()
}
fun ZonedDateTime.asPrettyString(): String = withZoneSameInstant(ZoneOffset.UTC).format(UTC_FORMATTER)