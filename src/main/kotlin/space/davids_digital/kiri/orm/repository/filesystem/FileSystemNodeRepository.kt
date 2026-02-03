package space.davids_digital.kiri.orm.repository.filesystem

import org.springframework.data.jpa.repository.JpaRepository
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemNodeEntity
import java.util.UUID

interface FileSystemNodeRepository : JpaRepository<FileSystemNodeEntity, UUID> {
    fun findByParentIdAndName(parentId: UUID, name: String): FileSystemNodeEntity?
}
