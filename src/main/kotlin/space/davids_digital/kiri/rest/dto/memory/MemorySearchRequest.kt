package space.davids_digital.kiri.rest.dto.memory

data class MemorySearchRequest(
    val query: String,
    val limit: Int = 20
)
