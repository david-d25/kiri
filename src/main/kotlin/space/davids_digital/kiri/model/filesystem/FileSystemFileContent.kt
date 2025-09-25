package space.davids_digital.kiri.model.filesystem

import java.util.UUID

data class FileSystemFileContent(
    val nodeId: UUID,
    val content: ByteArray,
)
