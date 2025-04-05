package space.davids_digital.kiri.orm.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.MemoryLinkEntity
import space.davids_digital.kiri.orm.entity.id.MemoryLinkEntityId
import java.time.OffsetDateTime
import java.util.*

@Repository
interface MemoryLinkRepository : JpaRepository<MemoryLinkEntity, MemoryLinkEntityId> {
    fun findByMemoryPointId(memoryPointId: UUID): List<MemoryLinkEntity>
    fun findByMemoryKeyId(memoryKeyId: UUID): List<MemoryLinkEntity>

    @Modifying
    @Query(
        """
            update kiri.memory_links
            set weight = :weight, last_updated_at = :lastUpdatedAt
            where memory_key_id = :memoryKeyId and memory_point_id = :memoryPointId 
        """,
        nativeQuery = true
    )
    fun updateWeightAndLastUpdatedAt(
        @Param("memoryKeyId") memoryKeyId: UUID,
        @Param("memoryPointId") memoryPointId: UUID,
        @Param("weight") weight: Double,
        @Param("lastUpdatedAt") lastUpdatedAt: OffsetDateTime
    )
}
