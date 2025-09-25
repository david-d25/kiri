package space.davids_digital.kiri.rest.dto.filesystem

data class FileSystemUpdateDirectoryRequest(
    val name: String? = null,
    val attributes: Map<String, String>? = null,
)
