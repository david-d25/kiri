package space.davids_digital.kiri.rest.dto.memory

data class MemoryPointCreateRequest(
    val value: String,
    val keys: List<String> = emptyList()
)
