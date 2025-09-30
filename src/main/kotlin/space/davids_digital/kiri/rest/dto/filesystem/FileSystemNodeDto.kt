package space.davids_digital.kiri.rest.dto.filesystem

import java.time.OffsetDateTime
import java.util.*

data class FileSystemNodeDto(
    val id: UUID,
    val spaceId: UUID,
    val parentId: UUID?,
    val name: String,
    val type: Type,
    val mimeType: String?,
    val size: Long,
    val attributes: Map<String, String>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    enum class Type {
        DIRECTORY,
        FILE,
    }
}