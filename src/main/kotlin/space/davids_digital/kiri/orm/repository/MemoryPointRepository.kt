package space.davids_digital.kiri.orm.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.MemoryPointEntity
import java.util.UUID

@Repository
interface MemoryPointRepository: JpaRepository<MemoryPointEntity, UUID> {
    fun findByValue(value: String): MemoryPointEntity?
    fun findByIdIn(ids: Collection<UUID>): List<MemoryPointEntity>
    fun findByValueContainingIgnoreCase(query: String, pageable: Pageable): Page<MemoryPointEntity>

    @Query(
        value = "SELECT id FROM main.memory_points WHERE replace(id::text, '-', '') LIKE :prefix || '%'",
        nativeQuery = true
    )
    fun findIdsByHexPrefix(@Param("prefix") prefix: String): List<UUID>
}