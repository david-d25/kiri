package space.davids_digital.kiri.orm.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "main", name = "memory_points")
class MemoryPointEntity {
    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()

    @Column(name = "value", unique = true)
    var value: String = ""

    @Column(name = "created_at")
    var createdAt: OffsetDateTime = OffsetDateTime.now()
}