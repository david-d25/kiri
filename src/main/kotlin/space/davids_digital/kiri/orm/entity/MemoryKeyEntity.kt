package space.davids_digital.kiri.orm.entity

import jakarta.persistence.*
import org.hibernate.annotations.Array
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(schema = "kiri", name = "memory_keys")
class MemoryKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()

    @Column(name = "key_text")
    var keyText: String = ""

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "embedding_model_id")
    var embeddingModel: EmbeddingModelEntity = EmbeddingModelEntity()

    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.OTHER)
    @Array(length = 2000)
    var embedding: FloatArray = FloatArray(0)
}