package space.davids_digital.kiri.agent.notification

import space.davids_digital.kiri.agent.frame.DataFrame.ContentPart
import java.time.ZonedDateTime

data class Notification(
    val content: List<ContentPart>,
    val sentAt: ZonedDateTime,
    val metadata: Map<String, String> = emptyMap(),
)