package space.davids_digital.kiri.orm.repository.filesystem

import org.springframework.data.jpa.repository.JpaRepository
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemSpaceEntity
import java.util.UUID

interface FileSystemSpaceRepository : JpaRepository<FileSystemSpaceEntity, UUID> {
    fun existsBySlugIgnoreCaseAndOwnerUserId(slug: String, ownerUserId: Long): Boolean
    fun existsBySlugIgnoreCaseAndOwnerUserIdIsNull(slug: String): Boolean
    fun findAllByOwnerUserId(ownerUserId: Long): List<FileSystemSpaceEntity>
    fun findAllByOwnerUserIdIsNull(): List<FileSystemSpaceEntity>
}
