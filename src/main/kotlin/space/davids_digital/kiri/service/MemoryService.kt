package space.davids_digital.kiri.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.integration.openai.OpenAiEmbeddingService
import space.davids_digital.kiri.model.MemoryKey
import space.davids_digital.kiri.model.MemoryPoint
import space.davids_digital.kiri.model.ScoredMemoryPoint
import space.davids_digital.kiri.orm.service.EmbeddingModelOrmService
import space.davids_digital.kiri.orm.service.MemoryOrmService
import java.time.ZonedDateTime
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@Service
class MemoryService(
    private val orm: MemoryOrmService,
    private val openAiEmbeddingService: OpenAiEmbeddingService,
    private val embeddingModelOrmService: EmbeddingModelOrmService
) {
    val decayRate: Double = 0.99
    val reinforcementFactor: Double = 0.05
    val minWeight: Double = 0.01
    val maxWeight: Double = 2.0

    @Transactional
    suspend fun getOrCreateKeys(texts: List<String>): List<MemoryKey> {
        if (texts.isEmpty()) {
            return emptyList()
        }
        val modelName = "text-embedding-3-large"
        val dimensionality = 2000
        val model = embeddingModelOrmService.getOrCreate(modelName, "openai", dimensionality)
        val existingKeys = texts.associate { it to orm.getMemoryKeyByText(it) }.toMutableMap()
        val missingTexts = texts.filter { existingKeys[it] == null }
        val newKeys = openAiEmbeddingService.createEmbeddings(modelName, missingTexts, dimensionality)
        missingTexts.forEachIndexed { index, memoryKey ->
            val embedding = newKeys[index]
            val key = orm.getOrCreateMemoryKey(memoryKey, embedding, model.id)
            existingKeys[memoryKey] = key
        }
        if (existingKeys.values.any { it == null }) {
            error("Couldn't create some of the memory keys")
        }
        return texts.map { existingKeys[it]!! }
    }

    suspend fun getOrCreatePoint(text: String): MemoryPoint {
        return orm.getOrCreateMemoryPoint(text)
    }

    @Transactional
    fun remember(keys: List<MemoryKey>, point: MemoryPoint, timestamp: ZonedDateTime) {
        keys.forEach { key ->
            orm.upsertMemoryLink(
                memoryKeyId = key.id,
                memoryPointId = point.id,
                weight = reinforcementFactor,
                timestamp = timestamp
            )
        }
    }

    @Transactional(readOnly = true)
    fun retrieve(keys: List<MemoryKey>, limit: Int): List<ScoredMemoryPoint> {
        val scoredPoints = mutableMapOf<MemoryPoint, Double>()

        // To avoid missing high-weight links that are slightly more distant.
        val expandedLimit = ceil(limit * maxWeight).toInt()

        val candidateKeyIds = mutableSetOf<java.util.UUID>()
        for (queryKey in keys) {
            val nearestKeys = orm.findNearestMemoryKeys(queryKey.embedding, expandedLimit)
            candidateKeyIds.addAll(nearestKeys.map { it.id })
        }
        val allLinks = orm.getMemoryLinksByMemoryKeys(candidateKeyIds)
        val pointIds = allLinks.map { it.memoryPointId }.toSet()
        val pointsMap = orm.getMemoryPointsByIds(pointIds).associateBy { it.id }
        val keysMap = orm.getMemoryKeysByIds(candidateKeyIds).associateBy { it.id }

        for (link in allLinks) {
            val point = pointsMap[link.memoryPointId] ?: continue
            val linkedKey = keysMap[link.memoryKeyId] ?: continue

            // We'll accumulate scores over all queryKeys
            for (queryKey in keys) {
                val distance = cosineDistance(queryKey.embedding, linkedKey.embedding)
                val score = calculateScore(distance, link.weight)
                scoredPoints.merge(point, score, Double::plus)
            }
        }

        return scoredPoints.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { ScoredMemoryPoint(it.key, it.value) }
    }

    @Transactional
    fun decayMemory() {
        val links = orm.getAllMemoryLinks()
        val now = ZonedDateTime.now()

        for (link in links) {
            val maxElapsed = 60 * 60 // 1 hour
            val elapsedSeconds = max(1L, now.toEpochSecond() - link.lastUpdatedAt.toEpochSecond())
            val effectiveElapsed = min(elapsedSeconds, maxElapsed.toLong())
            val decayed = link.weight * decayRate.pow(effectiveElapsed.toDouble())
            val clamped = clamp(decayed)

            orm.upsertMemoryLink(
                memoryKeyId = link.memoryKeyId,
                memoryPointId = link.memoryPointId,
                weight = clamped,
                timestamp = now
            )
        }
    }

    private fun calculateScore(distance: Double, weight: Double): Double {
        val epsilon = 1e-3
        return (1.0 / (distance + epsilon)) * weight
    }

    private fun clamp(weight: Double): Double {
        return min(maxWeight, max(minWeight, weight))
    }

    private fun cosineDistance(vec1: FloatArray, vec2: FloatArray): Double {
        val dotProduct = vec1.zip(vec2).map { it.first * it.second }.sum()
        val norm1 = kotlin.math.sqrt(vec1.map { it * it }.sum())
        val norm2 = kotlin.math.sqrt(vec2.map { it * it }.sum())
        return 1.0 - (dotProduct / (norm1 * norm2 + 1e-9))
    }
}