package space.davids_digital.kiri.orm.repository.filesystem

import org.springframework.data.jpa.repository.JpaRepository
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemFileContentEntity
import java.util.UUID

interface FileSystemFileContentRepository : JpaRepository<FileSystemFileContentEntity, UUID>
