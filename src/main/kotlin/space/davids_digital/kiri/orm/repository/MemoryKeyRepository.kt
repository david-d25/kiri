package space.davids_digital.kiri.orm.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.MemoryKeyEntity
import java.util.UUID

@Repository
interface MemoryKeyRepository : JpaRepository<MemoryKeyEntity, UUID> {
    fun findByKeyText(keyText: String): MemoryKeyEntity?

    @Query(
        """
        select * from kiri.memory_keys
        order by embedding <-> CAST(:embedding AS vector)
        limit :limit
        """,
        nativeQuery = true
    )
    fun findNearest(
        @Param("embedding") embedding: String,
        @Param("limit") limit: Int
    ): List<MemoryKeyEntity>
}