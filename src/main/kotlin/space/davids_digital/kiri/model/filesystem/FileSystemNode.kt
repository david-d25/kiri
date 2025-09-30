package space.davids_digital.kiri.model.filesystem

import java.time.ZonedDateTime
import java.util.UUID

data class FileSystemNode(
    val id: UUID,
    val spaceId: UUID,
    val parentId: UUID?,
    val name: String,
    val type: Type,
    val mimeType: String?,
    val size: Long,
    val attributes: Map<String, String>,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
) {
    enum class Type {
        DIRECTORY,
        FILE,
    }
}
