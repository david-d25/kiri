package space.davids_digital.kiri.model.filesystem

import java.time.OffsetDateTime
import java.util.UUID

data class FileSystemSpace(
    val id: UUID,
    val slug: String,
    val displayName: String,
    val description: String?,
    val ownerUserId: Long?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
