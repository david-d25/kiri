package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(schema = "kiri", name = "memory_points")
class MemoryPointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()

    @Column(name = "value", unique = true)
    var value: String = ""

    @Column(name = "created_at")
    var createdAt: OffsetDateTime = OffsetDateTime.now()
}