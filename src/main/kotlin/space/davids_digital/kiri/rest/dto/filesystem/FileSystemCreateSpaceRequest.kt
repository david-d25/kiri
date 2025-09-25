package space.davids_digital.kiri.rest.dto.filesystem

data class FileSystemCreateSpaceRequest(
    val slug: String,
    val displayName: String,
    val description: String? = null,
    val ownerUserId: Long? = null,
)
