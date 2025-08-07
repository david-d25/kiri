package space.davids_digital.kiri.orm.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(schema = "main", name = "memory_keys")
class MemoryKeyEntity {
    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()

    @Column(name = "key_text")
    var keyText: String = ""

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "embedding_model_id")
    var embeddingModel: EmbeddingModelEntity = EmbeddingModelEntity()

    @Column(name = "embedding")
    var embedding: FloatArray = FloatArray(0)
}