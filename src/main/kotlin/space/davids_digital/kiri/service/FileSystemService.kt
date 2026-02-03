package space.davids_digital.kiri.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.filesystem.FileSystemNode
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemFileContentEntity
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemNodeEntity
import space.davids_digital.kiri.orm.service.filesystem.FileSystemFileContentOrmService
import space.davids_digital.kiri.orm.service.filesystem.FileSystemNodeOrmService
import space.davids_digital.kiri.service.exception.ResourceNotFoundException
import space.davids_digital.kiri.service.exception.ValidationException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class FileSystemService(
    private val nodeOrm: FileSystemNodeOrmService,
    private val contentOrm: FileSystemFileContentOrmService,
) {
    private val log = LoggerFactory.getLogger(FileSystemService::class.java)

    @Transactional
    fun createDirectory(parentDir: String, name: String, attributes: Map<String, String> = emptyMap()): FileSystemNode {
        TODO()
    }

    @Transactional
    fun createFile(
        parentDir: String,
        name: String,
        content: ByteArray = byteArrayOf(),
        attributes: Map<String, String> = emptyMap()
    ): FileSystemNode {
        TODO()
    }

//    @Transactional
//    fun createDirectory(parentDir: String, name: String, attributes: Map<String, String> = emptyMap()): FileSystemNode {
//        val parentEntity = parentId?.let { ensureDirectory(spaceId, it) }
//        val trimmedName = name.trim()
//        if (trimmedName.isEmpty()) {
//            throw ValidationException("Directory name cannot be empty")
//        }
//        if (nodeOrm.existsWithName(spaceId, parentId, trimmedName)) {
//            throw ValidationException("Node with name '$trimmedName' already exists")
//        }
//        val now = OffsetDateTime.now()
//        val sanitizedAttributes = attributes.filterKeys { it.isNotBlank() }
//        val entity = FileSystemNodeEntity().apply {
//            id = UUID.randomUUID()
//            this.parentId = parentEntity?.id
//            this.name = trimmedName
//            type = FileSystemNodeEntity.NodeType.DIRECTORY
//            mimeType = null
//            size = 0
//            createdAt = now
//            updatedAt = now
//            this.attributes.clear()
//            this.attributes.putAll(sanitizedAttributes)
//        }
//        val saved = nodeOrm.save(entity)
//        log.info("Created directory '{}'", trimmedName)
//        return saved
//    }
//
//    @Transactional
//    fun createFile(
//        directory: String,
//        name: String,
//        mimeType: String?,
//        attributes: Map<String, String>,
//        content: ByteArray,
//    ): FileSystemNode {
//        val parentEntity = parentId?.let { ensureDirectory(spaceId, it) }
//        val trimmedName = name.trim()
//        if (trimmedName.isEmpty()) {
//            throw ValidationException("File name cannot be empty")
//        }
//        if (nodeOrm.existsWithName(spaceId, parentId, trimmedName)) {
//            throw ValidationException("Node with name '$trimmedName' already exists")
//        }
//        val now = OffsetDateTime.now()
//        val sanitizedAttributes = attributes.filterKeys { it.isNotBlank() }
//        val nodeEntity = FileSystemNodeEntity().apply {
//            id = UUID.randomUUID()
//            this.parentId = parentEntity?.id
//            this.name = trimmedName
//            type = FileSystemNodeEntity.NodeType.FILE
//            this.mimeType = mimeType?.takeIf { it.isNotBlank() }
//            size = content.size.toLong()
//            createdAt = now
//            updatedAt = now
//            this.attributes.clear()
//            this.attributes.putAll(sanitizedAttributes)
//        }
//        val savedNode = nodeOrm.save(nodeEntity)
//        val contentEntity = FileSystemFileContentEntity().apply {
//            nodeId = savedNode.id
//            this.content = content
//        }
//        contentOrm.save(contentEntity)
//        log.info("Created file '{}' ({} bytes) in space {}", trimmedName, content.size, spaceId)
//        return savedNode
//    }
//
//    @Transactional(readOnly = true)
//    fun listChildren(spaceId: UUID, parentId: UUID?): List<FileSystemNode> {
//        ensureSpaceExists(spaceId)
//        parentId?.let { ensureDirectory(spaceId, it) }
//        return nodeOrm.findChildren(spaceId, parentId)
//    }
//
//    @Transactional(readOnly = true)
//    fun getNode(spaceId: UUID, nodeId: UUID): FileSystemNode {
//        val node = nodeOrm.findById(nodeId) ?: throw ResourceNotFoundException("Node $nodeId not found")
//        if (node.spaceId != spaceId) {
//            throw ResourceNotFoundException("Node $nodeId not found in space $spaceId")
//        }
//        return node
//    }
//
//    @Transactional(readOnly = true)
//    fun getFileContent(nodeId: UUID): ByteArray {
//        val entity = nodeOrm.findEntityById(nodeId) ?: throw ResourceNotFoundException("Node $nodeId not found")
//        if (entity.type != FileSystemNodeEntity.NodeType.FILE) {
//            throw ValidationException("Node $nodeId is not a file")
//        }
//        val content = contentOrm.findById(nodeId) ?: throw ResourceNotFoundException("File $nodeId content not found")
//        return content.content
//    }
//
//    @Transactional
//    fun updateDirectory(
//        spaceId: UUID,
//        nodeId: UUID,
//        newName: String?,
//        attributes: Map<String, String>?,
//    ): FileSystemNode {
//        val entity = ensureDirectory(spaceId, nodeId)
//        var updated = false
//        newName?.let {
//            val trimmed = it.trim()
//            if (trimmed.isEmpty()) {
//                throw ValidationException("Directory name cannot be empty")
//            }
//            if (!trimmed.equals(entity.name, ignoreCase = true) &&
//                nodeOrm.existsWithName(spaceId, entity.parentId, trimmed)
//            ) {
//                throw ValidationException("Node with name '$trimmed' already exists")
//            }
//            if (trimmed != entity.name) {
//                entity.name = trimmed
//                updated = true
//            }
//        }
//        attributes?.let {
//            val sanitized = it.filterKeys { key -> key.isNotBlank() }
//            entity.attributes.clear()
//            entity.attributes.putAll(sanitized)
//            updated = true
//        }
//        if (updated) {
//            entity.updatedAt = OffsetDateTime.now()
//        }
//        return if (updated) {
//            nodeOrm.save(entity)
//        } else {
//            nodeOrm.findById(entity.id) ?: throw ResourceNotFoundException("Node $nodeId not found")
//        }
//    }
//
//    @Transactional
//    fun updateFile(
//        spaceId: UUID,
//        nodeId: UUID,
//        newName: String?,
//        mimeType: String?,
//        attributes: Map<String, String>?,
//        content: ByteArray?,
//    ): FileSystemNode {
//        val entity = ensureFile(spaceId, nodeId)
//        var updated = false
//        newName?.let {
//            val trimmed = it.trim()
//            if (trimmed.isEmpty()) {
//                throw ValidationException("File name cannot be empty")
//            }
//            if (!trimmed.equals(entity.name, ignoreCase = true) &&
//                nodeOrm.existsWithName(spaceId, entity.parentId, trimmed)
//            ) {
//                throw ValidationException("Node with name '$trimmed' already exists")
//            }
//            if (trimmed != entity.name) {
//                entity.name = trimmed
//                updated = true
//            }
//        }
//        mimeType?.let {
//            val trimmed = it.trim()
//            entity.mimeType = trimmed.ifEmpty { null }
//            updated = true
//        }
//        attributes?.let {
//            val sanitized = it.filterKeys { key -> key.isNotBlank() }
//            entity.attributes.clear()
//            entity.attributes.putAll(sanitized)
//            updated = true
//        }
//        content?.let {
//            val contentEntity = FileSystemFileContentEntity().apply {
//                this.nodeId = entity.id
//                this.content = it
//            }
//            contentOrm.save(contentEntity)
//            entity.size = it.size.toLong()
//            updated = true
//        }
//        if (updated) {
//            entity.updatedAt = OffsetDateTime.now()
//        }
//        return if (updated) {
//            nodeOrm.save(entity)
//        } else {
//            nodeOrm.findById(entity.id) ?: throw ResourceNotFoundException("Node $nodeId not found")
//        }
//    }
//
//    @Transactional
//    fun deleteNode(nodeId: UUID) {
//        val entity = nodeOrm.findEntityById(nodeId) ?: throw ResourceNotFoundException("Node $nodeId not found")
//        nodeOrm.delete(entity)
//        log.info("Deleted node {}", nodeId)
//    }
}
