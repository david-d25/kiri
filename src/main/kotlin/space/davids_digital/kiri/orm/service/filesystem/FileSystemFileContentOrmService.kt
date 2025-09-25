package space.davids_digital.kiri.orm.service.filesystem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.filesystem.FileSystemFileContent
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemFileContentEntity
import space.davids_digital.kiri.orm.mapper.filesystem.FileSystemFileContentEntityMapper
import space.davids_digital.kiri.orm.repository.filesystem.FileSystemFileContentRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class FileSystemFileContentOrmService(
    private val repo: FileSystemFileContentRepository,
    private val mapper: FileSystemFileContentEntityMapper,
) {
    @Transactional(readOnly = true)
    fun findById(id: UUID): FileSystemFileContent? = mapper.toModel(repo.findById(id).getOrNull())

    @Transactional
    fun save(entity: FileSystemFileContentEntity): FileSystemFileContent {
        return mapper.toModel(repo.save(entity))!!
    }

    @Transactional
    fun deleteById(id: UUID) {
        repo.deleteById(id)
    }
}
