package space.davids_digital.kiri.orm.repository.filesystem

import org.springframework.data.jpa.repository.JpaRepository
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemNodeEntity
import java.util.UUID

interface FileSystemNodeRepository : JpaRepository<FileSystemNodeEntity, UUID> {
    fun findAllBySpaceIdAndParentId(spaceId: UUID, parentId: UUID): List<FileSystemNodeEntity>
    fun findAllBySpaceIdAndParentIdIsNull(spaceId: UUID): List<FileSystemNodeEntity>
    fun existsBySpaceIdAndParentIdAndNameIgnoreCase(spaceId: UUID, parentId: UUID, name: String): Boolean
    fun existsBySpaceIdAndParentIdIsNullAndNameIgnoreCase(spaceId: UUID, name: String): Boolean
}
