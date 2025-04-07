package space.davids_digital.kiri.orm.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.MemoryPointEntity
import java.util.UUID

@Repository
interface MemoryPointRepository: JpaRepository<MemoryPointEntity, UUID> {
    fun findByValue(value: String): MemoryPointEntity?
    fun findByIdIn(ids: Collection<UUID>): List<MemoryPointEntity>
}