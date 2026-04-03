package space.davids_digital.kiri.rest.controller

import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import space.davids_digital.kiri.orm.service.MemoryOrmService
import space.davids_digital.kiri.rest.dto.memory.*
import space.davids_digital.kiri.service.MemoryService
import java.time.ZonedDateTime
import java.util.UUID

@RestController
@RequestMapping("/memory")
class MemoryController(
    private val memoryService: MemoryService,
    private val memoryOrmService: MemoryOrmService
) {
    @GetMapping("/stats")
    fun getStats(): MemoryStatsDto {
        return MemoryStatsDto(
            totalPoints = memoryOrmService.countMemoryPoints(),
            totalKeys = memoryOrmService.countMemoryKeys(),
            totalLinks = memoryOrmService.countMemoryLinks()
        )
    }

    @GetMapping("/points")
    fun getPoints(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): Page<MemoryPointDto> {
        val pointsPage = if (!search.isNullOrBlank()) {
            memoryOrmService.searchMemoryPointsByText(search, page, size)
        } else {
            memoryOrmService.getMemoryPointsPaged(page, size)
        }
        val pointIds = pointsPage.content.map { it.id }.toSet()
        val linksCounts = memoryOrmService.countLinksByMemoryPoints(pointIds)
        return pointsPage.map { point ->
            MemoryPointDto(
                id = point.id,
                value = point.value,
                createdAt = point.createdAt,
                linkedKeysCount = linksCounts[point.id] ?: 0
            )
        }
    }

    @GetMapping("/points/{id}")
    fun getPointDetail(@PathVariable id: UUID): MemoryPointDetailDto {
        val point = memoryOrmService.getMemoryPoint(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Memory point not found: $id")
        val links = memoryOrmService.getMemoryLinksByMemoryPoint(id)
        val keyIds = links.map { it.memoryKeyId }.toSet()
        val keysMap = memoryOrmService.getMemoryKeysByIds(keyIds).associateBy { it.id }

        return MemoryPointDetailDto(
            id = point.id,
            value = point.value,
            createdAt = point.createdAt,
            links = links.map { link ->
                MemoryLinkDto(
                    memoryKeyId = link.memoryKeyId,
                    memoryPointId = link.memoryPointId,
                    weight = link.weight,
                    lastUpdatedAt = link.lastUpdatedAt,
                    keyText = keysMap[link.memoryKeyId]?.keyText,
                    pointValue = null
                )
            }
        )
    }

    @PostMapping("/points")
    suspend fun createPoint(@RequestBody request: MemoryPointCreateRequest): MemoryPointDetailDto {
        val point = memoryService.getOrCreatePoint(request.value)
        if (request.keys.isNotEmpty()) {
            val keys = memoryService.getOrCreateKeys(request.keys)
            memoryService.remember(keys, point, ZonedDateTime.now())
        }
        return getPointDetail(point.id)
    }

    @PutMapping("/points/{id}")
    fun updatePoint(@PathVariable id: UUID, @RequestBody request: MemoryPointUpdateRequest): MemoryPointDto {
        val updated = memoryOrmService.updateMemoryPointValue(id, request.value)
        val linksCount = memoryOrmService.countLinksByMemoryPoints(setOf(id))[id] ?: 0
        return MemoryPointDto(
            id = updated.id,
            value = updated.value,
            createdAt = updated.createdAt,
            linkedKeysCount = linksCount
        )
    }

    @DeleteMapping("/points/{id}")
    fun deletePoint(@PathVariable id: UUID) {
        memoryOrmService.deleteMemoryPoint(id)
    }

    @GetMapping("/keys")
    fun getKeys(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): Map<String, Any> {
        val (keys, total) = if (!search.isNullOrBlank()) {
            memoryOrmService.searchMemoryKeysByText(search, page, size)
        } else {
            memoryOrmService.getMemoryKeysPaged(page, size)
        }
        val keyIds = keys.map { it.id }.toSet()
        val linksCounts = memoryOrmService.countLinksByMemoryKeys(keyIds)

        return mapOf(
            "content" to keys.map { key ->
                MemoryKeyDto(
                    id = key.id,
                    keyText = key.keyText,
                    embeddingModel = "${key.embeddingModel.vendor}/${key.embeddingModel.name}",
                    linkedPointsCount = linksCounts[key.id] ?: 0
                )
            },
            "totalElements" to total,
            "totalPages" to ((total + size - 1) / size),
            "number" to page,
            "size" to size
        )
    }

    @DeleteMapping("/keys/{id}")
    fun deleteKey(@PathVariable id: UUID) {
        memoryOrmService.deleteMemoryKey(id)
    }

    @PutMapping("/links/{keyId}/{pointId}")
    fun updateLink(
        @PathVariable keyId: UUID,
        @PathVariable pointId: UUID,
        @RequestBody request: MemoryLinkUpdateRequest
    ): MemoryLinkDto {
        val now = ZonedDateTime.now()
        memoryOrmService.upsertMemoryLink(keyId, pointId, request.weight, now)
        val key = memoryOrmService.getMemoryKeysByIds(listOf(keyId)).firstOrNull()
        val point = memoryOrmService.getMemoryPoint(pointId)
        return MemoryLinkDto(
            memoryKeyId = keyId,
            memoryPointId = pointId,
            weight = request.weight,
            lastUpdatedAt = now,
            keyText = key?.keyText,
            pointValue = point?.value
        )
    }

    @DeleteMapping("/links/{keyId}/{pointId}")
    fun deleteLink(@PathVariable keyId: UUID, @PathVariable pointId: UUID) {
        memoryOrmService.deleteMemoryLink(keyId, pointId)
    }

    @PostMapping("/search")
    suspend fun semanticSearch(@RequestBody request: MemorySearchRequest): List<MemorySearchResultDto> {
        val embeddings = memoryService.createEmbeddings(listOf(request.query))
        val results = memoryService.retrieveByEmbeddings(embeddings, request.limit)
        val pointIds = results.map { it.point.id }.toSet()
        val linksCounts = memoryOrmService.countLinksByMemoryPoints(pointIds)
        return results.map { scoredPoint ->
            MemorySearchResultDto(
                point = MemoryPointDto(
                    id = scoredPoint.point.id,
                    value = scoredPoint.point.value,
                    createdAt = scoredPoint.point.createdAt,
                    linkedKeysCount = linksCounts[scoredPoint.point.id] ?: 0
                ),
                score = scoredPoint.score
            )
        }
    }
}
