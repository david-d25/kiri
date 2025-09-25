package space.davids_digital.kiri.rest.controller.filesystem

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.model.filesystem.FileSystemNode
import space.davids_digital.kiri.model.filesystem.FileSystemSpace
import space.davids_digital.kiri.rest.dto.filesystem.FileSystemCreateDirectoryRequest
import space.davids_digital.kiri.rest.dto.filesystem.FileSystemCreateFileRequest
import space.davids_digital.kiri.rest.dto.filesystem.FileSystemCreateSpaceRequest
import space.davids_digital.kiri.rest.dto.filesystem.FileSystemNodeDto
import space.davids_digital.kiri.rest.dto.filesystem.FileSystemSpaceDto
import space.davids_digital.kiri.rest.dto.filesystem.FileSystemUpdateDirectoryRequest
import space.davids_digital.kiri.rest.dto.filesystem.FileSystemUpdateFileRequest
import space.davids_digital.kiri.service.FileSystemService
import space.davids_digital.kiri.service.exception.ValidationException
import java.util.Base64
import java.util.UUID

@RestController
@RequestMapping("/filesystem")
class FileSystemController(
    private val service: FileSystemService,
) {
    @PostMapping("/spaces")
    fun createSpace(@RequestBody request: FileSystemCreateSpaceRequest): FileSystemSpaceDto {
        val space = service.createSpace(request.slug, request.displayName, request.description, request.ownerUserId)
        return space.toDto()
    }

    @GetMapping("/spaces")
    fun listSpaces(@RequestParam(required = false) ownerId: Long?): List<FileSystemSpaceDto> {
        return service.listSpaces(ownerId).map { it.toDto() }
    }

    @GetMapping("/spaces/{spaceId}")
    fun getSpace(@PathVariable spaceId: UUID): FileSystemSpaceDto {
        return service.getSpace(spaceId).toDto()
    }

    @PostMapping("/spaces/{spaceId}/directories")
    fun createDirectory(
        @PathVariable spaceId: UUID,
        @RequestBody request: FileSystemCreateDirectoryRequest,
    ): FileSystemNodeDto {
        val node = service.createDirectory(spaceId, request.parentId, request.name, request.attributes)
        return node.toDto()
    }

    @PostMapping("/spaces/{spaceId}/files")
    fun createFile(
        @PathVariable spaceId: UUID,
        @RequestBody request: FileSystemCreateFileRequest,
    ): FileSystemNodeDto {
        val content = decodeContent(request.contentBase64)
        val node = service.createFile(spaceId, request.parentId, request.name, request.mimeType, request.attributes, content)
        return node.toDto()
    }

    @GetMapping("/spaces/{spaceId}/nodes")
    fun listNodes(
        @PathVariable spaceId: UUID,
        @RequestParam(required = false) parentId: UUID?,
    ): List<FileSystemNodeDto> {
        return service.listChildren(spaceId, parentId).map { it.toDto() }
    }

    @GetMapping("/spaces/{spaceId}/nodes/{nodeId}")
    fun getNode(
        @PathVariable spaceId: UUID,
        @PathVariable nodeId: UUID,
    ): FileSystemNodeDto {
        return service.getNode(spaceId, nodeId).toDto()
    }

    @GetMapping("/spaces/{spaceId}/files/{nodeId}/content")
    fun getFileContent(
        @PathVariable spaceId: UUID,
        @PathVariable nodeId: UUID,
    ): ResponseEntity<ByteArray> {
        val node = service.getNode(spaceId, nodeId)
        if (node.type != FileSystemNode.Type.FILE) {
            throw ValidationException("Node $nodeId is not a file")
        }
        val content = service.getFileContent(nodeId)
        val mediaType = node.mimeType?.let { safeMediaType(it) } ?: MediaType.APPLICATION_OCTET_STREAM
        val headers = HttpHeaders()
        headers.contentType = mediaType
        headers.contentLength = content.size.toLong()
        val safeName = node.name.replace('"', '_')
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$safeName\"")
        return ResponseEntity.ok().headers(headers).body(content)
    }

    @PutMapping("/spaces/{spaceId}/directories/{nodeId}")
    fun updateDirectory(
        @PathVariable spaceId: UUID,
        @PathVariable nodeId: UUID,
        @RequestBody request: FileSystemUpdateDirectoryRequest,
    ): FileSystemNodeDto {
        val node = service.updateDirectory(spaceId, nodeId, request.name, request.attributes)
        return node.toDto()
    }

    @PutMapping("/spaces/{spaceId}/files/{nodeId}")
    fun updateFile(
        @PathVariable spaceId: UUID,
        @PathVariable nodeId: UUID,
        @RequestBody request: FileSystemUpdateFileRequest,
    ): FileSystemNodeDto {
        val content = request.contentBase64?.let { decodeContent(it) }
        val node = service.updateFile(spaceId, nodeId, request.name, request.mimeType, request.attributes, content)
        return node.toDto()
    }

    @DeleteMapping("/spaces/{spaceId}/nodes/{nodeId}")
    fun deleteNode(
        @PathVariable spaceId: UUID,
        @PathVariable nodeId: UUID,
    ) {
        service.deleteNode(spaceId, nodeId)
    }

    private fun decodeContent(base64: String): ByteArray {
        return try {
            Base64.getDecoder().decode(base64)
        } catch (ex: IllegalArgumentException) {
            throw ValidationException("Invalid base64 content")
        }
    }

    private fun safeMediaType(value: String): MediaType {
        return try {
            MediaType.parseMediaType(value)
        } catch (ex: IllegalArgumentException) {
            MediaType.APPLICATION_OCTET_STREAM
        }
    }

    private fun FileSystemSpace.toDto(): FileSystemSpaceDto {
        return FileSystemSpaceDto(
            id = id,
            slug = slug,
            displayName = displayName,
            description = description,
            ownerUserId = ownerUserId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun FileSystemNode.toDto(): FileSystemNodeDto {
        return FileSystemNodeDto(
            id = id,
            spaceId = spaceId,
            parentId = parentId,
            name = name,
            type = type,
            mimeType = mimeType,
            size = size,
            attributes = attributes,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
