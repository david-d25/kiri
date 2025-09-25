package space.davids_digital.kiri.orm.service.filesystem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.filesystem.FileSystemNode
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemNodeEntity
import space.davids_digital.kiri.orm.mapper.filesystem.FileSystemNodeEntityMapper
import space.davids_digital.kiri.orm.repository.filesystem.FileSystemNodeRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class FileSystemNodeOrmService(
    private val repo: FileSystemNodeRepository,
    private val mapper: FileSystemNodeEntityMapper,
) {
    @Transactional(readOnly = true)
    fun findById(id: UUID): FileSystemNode? = mapper.toModel(repo.findById(id).getOrNull())

    @Transactional(readOnly = true)
    fun findEntityById(id: UUID): FileSystemNodeEntity? = repo.findById(id).getOrNull()

    @Transactional(readOnly = true)
    fun findChildren(spaceId: UUID, parentId: UUID?): List<FileSystemNode> {
        val entities = if (parentId == null) {
            repo.findAllBySpaceIdAndParentIdIsNull(spaceId)
        } else {
            repo.findAllBySpaceIdAndParentId(spaceId, parentId)
        }
        return entities.mapNotNull(mapper::toModel)
    }

    @Transactional
    fun save(entity: FileSystemNodeEntity): FileSystemNode {
        return mapper.toModel(repo.save(entity))!!
    }

    @Transactional
    fun delete(entity: FileSystemNodeEntity) {
        repo.delete(entity)
    }

    @Transactional(readOnly = true)
    fun existsWithName(spaceId: UUID, parentId: UUID?, name: String): Boolean {
        return if (parentId == null) {
            repo.existsBySpaceIdAndParentIdIsNullAndNameIgnoreCase(spaceId, name)
        } else {
            repo.existsBySpaceIdAndParentIdAndNameIgnoreCase(spaceId, parentId, name)
        }
    }
}
