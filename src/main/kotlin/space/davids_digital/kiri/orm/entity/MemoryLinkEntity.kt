package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import space.davids_digital.kiri.orm.entity.id.MemoryLinkEntityId
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(schema = "main", name = "memory_links")
@IdClass(MemoryLinkEntityId::class)
class MemoryLinkEntity {
    @Id
    @Column(name = "memory_key_id")
    var memoryKeyId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Id
    @Column(name = "memory_point_id")
    var memoryPointId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Column(name = "weight")
    var weight: Double = 0.0

    @Column(name = "last_updated_at")
    var lastUpdatedAt: OffsetDateTime = OffsetDateTime.now()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memory_key_id", insertable = false, updatable = false)
    val memoryKey: MemoryKeyEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memory_point_id", insertable = false, updatable = false)
    val memoryPoint: MemoryPointEntity? = null
}