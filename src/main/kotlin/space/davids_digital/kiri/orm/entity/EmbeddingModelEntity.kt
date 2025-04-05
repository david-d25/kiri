package space.davids_digital.kiri.orm.entity

import jakarta.persistence.*

@Entity
@Table(schema = "kiri", name = "embedding_models")
class EmbeddingModelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0

    @Column(name = "name")
    var name: String = ""

    @Column(name = "vendor")
    var vendor: String = ""

    @Column(name = "dimensionality")
    var dimensionality: Int = 0
}
