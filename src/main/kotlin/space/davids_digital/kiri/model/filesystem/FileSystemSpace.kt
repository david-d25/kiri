package space.davids_digital.kiri.model.filesystem

import java.time.ZonedDateTime
import java.util.*

data class FileSystemSpace(
    val id: UUID,
    val slug: String,
    val displayName: String,
    val description: String?,
    val ownerUserId: Long?,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
)
