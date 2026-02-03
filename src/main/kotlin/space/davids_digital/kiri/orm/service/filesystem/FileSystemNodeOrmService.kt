package space.davids_digital.kiri.orm.service.filesystem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.filesystem.FileSystemNode
import space.davids_digital.kiri.orm.mapper.filesystem.FileSystemNodeEntityMapper
import space.davids_digital.kiri.orm.repository.filesystem.FileSystemNodeRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class FileSystemNodeOrmService(
    private val repo: FileSystemNodeRepository,
    private val mapper: FileSystemNodeEntityMapper,
) {
    private val rootId = UUID.fromString("00000000-0000-0000-0000-000000000000")

    @Transactional(readOnly = true)
    fun findById(id: UUID): FileSystemNode? = mapper.toModel(repo.findById(id).getOrNull())

    /**
     * Find a file node by its path.
     * The path is expected to be a '/' separated string of node names.
     * The root node is represented by an empty string or '/'.
     */
    @Transactional(readOnly = true)
    fun findByPath(path: String): FileSystemNode? {
        val trimmedPath = path.trim().trimStart('/').trimEnd('/')
        if (trimmedPath.isEmpty()) {
            return findById(rootId)
        }
        val names = trimmedPath.split('/')
        var currentNode = findById(rootId)
        for (name in names) {
            currentNode = currentNode?.let { parent ->
                repo.findByParentIdAndName(parent.id, name)?.let { mapper.toModel(it) }
            }
            if (currentNode == null) {
                return null
            }
        }
        return currentNode
    }
}
