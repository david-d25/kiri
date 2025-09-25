package space.davids_digital.kiri.rest.dto.filesystem

data class FileSystemUpdateFileRequest(
    val name: String? = null,
    val mimeType: String? = null,
    val attributes: Map<String, String>? = null,
    val contentBase64: String? = null,
)
