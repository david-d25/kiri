package space.davids_digital.kiri.rest.dto.filesystem

import java.util.UUID

data class FileSystemCreateDirectoryRequest(
    val parentId: UUID? = null,
    val name: String,
    val attributes: Map<String, String> = emptyMap(),
)
