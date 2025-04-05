package space.davids_digital.kiri.model

data class EmbeddingModel(
    val id: Long,
    val name: String,
    val vendor: String,
    val dimensionality: Int
)