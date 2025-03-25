package space.davids_digital.kiri.agent.frame

import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val DATETIME_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'")

fun StaticDataFrame.Builder.addCreatedAtNow() {
    attributes["created-at"] = ZonedDateTime.now(ZoneOffset.UTC).asPrettyString()
}
fun ZonedDateTime.asPrettyString() = format(DATETIME_PATTERN)