package space.davids_digital.kiri.rest.dto.filesystem

import space.davids_digital.kiri.model.filesystem.FileSystemNode
import java.time.OffsetDateTime
import java.util.UUID

data class FileSystemNodeDto(
    val id: UUID,
    val spaceId: UUID,
    val parentId: UUID?,
    val name: String,
    val type: FileSystemNode.Type,
    val mimeType: String?,
    val size: Long,
    val attributes: Map<String, String>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
