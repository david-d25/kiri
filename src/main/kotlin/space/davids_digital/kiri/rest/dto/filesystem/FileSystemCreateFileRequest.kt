package space.davids_digital.kiri.rest.dto.filesystem

import java.util.UUID

data class FileSystemCreateFileRequest(
    val parentId: UUID? = null,
    val name: String,
    val mimeType: String? = null,
    val attributes: Map<String, String> = emptyMap(),
    val contentBase64: String,
)
