package space.davids_digital.kiri.model

import java.util.UUID

data class MemoryKey(
    val id: UUID,
    val keyText: String,
    val embeddingModel: EmbeddingModel,
    val embedding: FloatArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemoryKey

        if (id != other.id) return false
        if (keyText != other.keyText) return false
        if (embeddingModel != other.embeddingModel) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + keyText.hashCode()
        result = 31 * result + embeddingModel.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}