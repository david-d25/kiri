package space.davids_digital.kiri.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.filesystem.FileSystemNode
import space.davids_digital.kiri.model.filesystem.FileSystemSpace
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemFileContentEntity
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemNodeEntity
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemSpaceEntity
import space.davids_digital.kiri.orm.service.filesystem.FileSystemFileContentOrmService
import space.davids_digital.kiri.orm.service.filesystem.FileSystemNodeOrmService
import space.davids_digital.kiri.orm.service.filesystem.FileSystemSpaceOrmService
import space.davids_digital.kiri.service.exception.ResourceNotFoundException
import space.davids_digital.kiri.service.exception.ValidationException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class FileSystemService(
    private val spaceOrm: FileSystemSpaceOrmService,
    private val nodeOrm: FileSystemNodeOrmService,
    private val contentOrm: FileSystemFileContentOrmService,
) {
    private val log = LoggerFactory.getLogger(FileSystemService::class.java)

    @Transactional
    fun createSpace(slug: String, displayName: String, description: String?, ownerUserId: Long?): FileSystemSpace {
        val trimmedSlug = slug.trim()
        val trimmedName = displayName.trim()
        if (trimmedSlug.isEmpty()) {
            throw ValidationException("Space slug cannot be empty")
        }
        if (trimmedName.isEmpty()) {
            throw ValidationException("Space display name cannot be empty")
        }
        if (ownerUserId != null) {
            if (spaceOrm.existsBySlugForOwner(trimmedSlug, ownerUserId)) {
                throw ValidationException("Space with slug '$trimmedSlug' already exists for owner $ownerUserId")
            }
        } else if (spaceOrm.existsBySlugForGlobal(trimmedSlug)) {
            throw ValidationException("Global space with slug '$trimmedSlug' already exists")
        }

        val now = OffsetDateTime.now()
        val entity = FileSystemSpaceEntity().apply {
            id = UUID.randomUUID()
            slug = trimmedSlug
            displayName = trimmedName
            this.description = description?.takeIf { it.isNotBlank() }
            this.ownerUserId = ownerUserId
            createdAt = now
            updatedAt = now
        }
        val saved = spaceOrm.save(entity)
        log.info("Created file system space '{}' (id={})", trimmedSlug, saved.id)
        return saved
    }

    @Transactional(readOnly = true)
    fun listSpaces(ownerUserId: Long?): List<FileSystemSpace> {
        return if (ownerUserId == null) {
            spaceOrm.findAll()
        } else {
            val globalSpaces = spaceOrm.findAllGlobal()
            val ownedSpaces = spaceOrm.findAllByOwner(ownerUserId)
            (ownedSpaces + globalSpaces).distinctBy { it.id }
        }
    }

    @Transactional(readOnly = true)
    fun getSpace(spaceId: UUID): FileSystemSpace {
        return spaceOrm.findById(spaceId) ?: throw ResourceNotFoundException("Space $spaceId not found")
    }

    @Transactional
    fun createDirectory(spaceId: UUID, parentId: UUID?, name: String, attributes: Map<String, String>): FileSystemNode {
        ensureSpaceExists(spaceId)
        val parentEntity = parentId?.let { ensureDirectory(spaceId, it) }
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            throw ValidationException("Directory name cannot be empty")
        }
        if (nodeOrm.existsWithName(spaceId, parentId, trimmedName)) {
            throw ValidationException("Node with name '$trimmedName' already exists")
        }
        val now = OffsetDateTime.now()
        val sanitizedAttributes = attributes.filterKeys { it.isNotBlank() }
        val entity = FileSystemNodeEntity().apply {
            id = UUID.randomUUID()
            this.spaceId = spaceId
            this.parentId = parentEntity?.id
            this.name = trimmedName
            type = FileSystemNodeEntity.NodeType.DIRECTORY
            mimeType = null
            size = 0
            createdAt = now
            updatedAt = now
            this.attributes.clear()
            this.attributes.putAll(sanitizedAttributes)
        }
        val saved = nodeOrm.save(entity)
        log.info("Created directory '{}' in space {}", trimmedName, spaceId)
        return saved
    }

    @Transactional
    fun createFile(
        spaceId: UUID,
        parentId: UUID?,
        name: String,
        mimeType: String?,
        attributes: Map<String, String>,
        content: ByteArray,
    ): FileSystemNode {
        ensureSpaceExists(spaceId)
        val parentEntity = parentId?.let { ensureDirectory(spaceId, it) }
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            throw ValidationException("File name cannot be empty")
        }
        if (nodeOrm.existsWithName(spaceId, parentId, trimmedName)) {
            throw ValidationException("Node with name '$trimmedName' already exists")
        }
        val now = OffsetDateTime.now()
        val sanitizedAttributes = attributes.filterKeys { it.isNotBlank() }
        val nodeEntity = FileSystemNodeEntity().apply {
            id = UUID.randomUUID()
            this.spaceId = spaceId
            this.parentId = parentEntity?.id
            this.name = trimmedName
            type = FileSystemNodeEntity.NodeType.FILE
            this.mimeType = mimeType?.takeIf { it.isNotBlank() }
            size = content.size.toLong()
            createdAt = now
            updatedAt = now
            this.attributes.clear()
            this.attributes.putAll(sanitizedAttributes)
        }
        val savedNode = nodeOrm.save(nodeEntity)
        val contentEntity = FileSystemFileContentEntity().apply {
            nodeId = savedNode.id
            this.content = content
        }
        contentOrm.save(contentEntity)
        log.info("Created file '{}' ({} bytes) in space {}", trimmedName, content.size, spaceId)
        return savedNode
    }

    @Transactional(readOnly = true)
    fun listChildren(spaceId: UUID, parentId: UUID?): List<FileSystemNode> {
        ensureSpaceExists(spaceId)
        parentId?.let { ensureDirectory(spaceId, it) }
        return nodeOrm.findChildren(spaceId, parentId)
    }

    @Transactional(readOnly = true)
    fun getNode(spaceId: UUID, nodeId: UUID): FileSystemNode {
        val node = nodeOrm.findById(nodeId) ?: throw ResourceNotFoundException("Node $nodeId not found")
        if (node.spaceId != spaceId) {
            throw ResourceNotFoundException("Node $nodeId not found in space $spaceId")
        }
        return node
    }

    @Transactional(readOnly = true)
    fun getFileContent(nodeId: UUID): ByteArray {
        val entity = nodeOrm.findEntityById(nodeId) ?: throw ResourceNotFoundException("Node $nodeId not found")
        if (entity.type != FileSystemNodeEntity.NodeType.FILE) {
            throw ValidationException("Node $nodeId is not a file")
        }
        val content = contentOrm.findById(nodeId) ?: throw ResourceNotFoundException("File $nodeId content not found")
        return content.content
    }

    @Transactional
    fun updateDirectory(
        spaceId: UUID,
        nodeId: UUID,
        newName: String?,
        attributes: Map<String, String>?,
    ): FileSystemNode {
        val entity = ensureDirectory(spaceId, nodeId)
        var updated = false
        newName?.let {
            val trimmed = it.trim()
            if (trimmed.isEmpty()) {
                throw ValidationException("Directory name cannot be empty")
            }
            if (!trimmed.equals(entity.name, ignoreCase = true) &&
                nodeOrm.existsWithName(spaceId, entity.parentId, trimmed)
            ) {
                throw ValidationException("Node with name '$trimmed' already exists")
            }
            if (trimmed != entity.name) {
                entity.name = trimmed
                updated = true
            }
        }
        attributes?.let {
            val sanitized = it.filterKeys { key -> key.isNotBlank() }
            entity.attributes.clear()
            entity.attributes.putAll(sanitized)
            updated = true
        }
        if (updated) {
            entity.updatedAt = OffsetDateTime.now()
        }
        return if (updated) {
            nodeOrm.save(entity)
        } else {
            nodeOrm.findById(entity.id) ?: throw ResourceNotFoundException("Node $nodeId not found")
        }
    }

    @Transactional
    fun updateFile(
        spaceId: UUID,
        nodeId: UUID,
        newName: String?,
        mimeType: String?,
        attributes: Map<String, String>?,
        content: ByteArray?,
    ): FileSystemNode {
        val entity = ensureFile(spaceId, nodeId)
        var updated = false
        newName?.let {
            val trimmed = it.trim()
            if (trimmed.isEmpty()) {
                throw ValidationException("File name cannot be empty")
            }
            if (!trimmed.equals(entity.name, ignoreCase = true) &&
                nodeOrm.existsWithName(spaceId, entity.parentId, trimmed)
            ) {
                throw ValidationException("Node with name '$trimmed' already exists")
            }
            if (trimmed != entity.name) {
                entity.name = trimmed
                updated = true
            }
        }
        mimeType?.let {
            val trimmed = it.trim()
            entity.mimeType = trimmed.ifEmpty { null }
            updated = true
        }
        attributes?.let {
            val sanitized = it.filterKeys { key -> key.isNotBlank() }
            entity.attributes.clear()
            entity.attributes.putAll(sanitized)
            updated = true
        }
        content?.let {
            val contentEntity = FileSystemFileContentEntity().apply {
                nodeId = entity.id
                this.content = it
            }
            contentOrm.save(contentEntity)
            entity.size = it.size.toLong()
            updated = true
        }
        if (updated) {
            entity.updatedAt = OffsetDateTime.now()
        }
        return if (updated) {
            nodeOrm.save(entity)
        } else {
            nodeOrm.findById(entity.id) ?: throw ResourceNotFoundException("Node $nodeId not found")
        }
    }

    @Transactional
    fun deleteNode(spaceId: UUID, nodeId: UUID) {
        val entity = ensureNodeBelongsToSpace(spaceId, nodeId)
        nodeOrm.delete(entity)
        log.info("Deleted node {} from space {}", nodeId, spaceId)
    }

    private fun ensureSpaceExists(spaceId: UUID): FileSystemSpace {
        return spaceOrm.findById(spaceId) ?: throw ResourceNotFoundException("Space $spaceId not found")
    }

    private fun ensureNodeBelongsToSpace(spaceId: UUID, nodeId: UUID): FileSystemNodeEntity {
        val entity = nodeOrm.findEntityById(nodeId) ?: throw ResourceNotFoundException("Node $nodeId not found")
        if (entity.spaceId != spaceId) {
            throw ResourceNotFoundException("Node $nodeId not found in space $spaceId")
        }
        return entity
    }

    private fun ensureDirectory(spaceId: UUID, nodeId: UUID): FileSystemNodeEntity {
        val entity = ensureNodeBelongsToSpace(spaceId, nodeId)
        if (entity.type != FileSystemNodeEntity.NodeType.DIRECTORY) {
            throw ValidationException("Node $nodeId is not a directory")
        }
        return entity
    }

    private fun ensureFile(spaceId: UUID, nodeId: UUID): FileSystemNodeEntity {
        val entity = ensureNodeBelongsToSpace(spaceId, nodeId)
        if (entity.type != FileSystemNodeEntity.NodeType.FILE) {
            throw ValidationException("Node $nodeId is not a file")
        }
        return entity
    }
}
