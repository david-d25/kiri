package space.davids_digital.kiri.orm.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.MemoryKey
import space.davids_digital.kiri.model.MemoryLink
import space.davids_digital.kiri.model.MemoryPoint
import space.davids_digital.kiri.orm.entity.MemoryKeyEntity
import space.davids_digital.kiri.orm.entity.MemoryLinkEntity
import space.davids_digital.kiri.orm.entity.MemoryPointEntity
import space.davids_digital.kiri.orm.entity.id.MemoryLinkEntityId
import space.davids_digital.kiri.orm.mapping.MemoryKeyEntityMapper
import space.davids_digital.kiri.orm.mapping.MemoryLinkEntityMapper
import space.davids_digital.kiri.orm.mapping.MemoryPointEntityMapper
import space.davids_digital.kiri.orm.repository.EmbeddingModelRepository
import space.davids_digital.kiri.orm.repository.MemoryKeyRepository
import space.davids_digital.kiri.orm.repository.MemoryLinkRepository
import space.davids_digital.kiri.orm.repository.MemoryPointRepository
import java.time.ZonedDateTime
import java.util.*

@Service
class MemoryOrmService(
    private val memoryKeyRepository: MemoryKeyRepository,
    private val memoryPointRepository: MemoryPointRepository,
    private val memoryLinkRepository: MemoryLinkRepository,
    private val embeddingModelRepository: EmbeddingModelRepository,
    private val memoryKeyMapper: MemoryKeyEntityMapper,
    private val memoryPointMapper: MemoryPointEntityMapper,
    private val memoryLinkMapper: MemoryLinkEntityMapper,
) {
    @Transactional
    fun getAllMemoryLinks(): List<MemoryLink> {
        return memoryLinkRepository.findAll().mapNotNull(memoryLinkMapper::toModel)
    }

    @Transactional
    fun getMemoryPoint(id: UUID): MemoryPoint? {
        return memoryPointMapper.toModel(memoryPointRepository.findById(id).orElse(null))
    }

    @Transactional
    fun getMemoryLinksByMemoryKeys(keys: Collection<UUID>): List<MemoryLink> {
        return memoryLinkRepository.findByMemoryKeyIdIn(keys).mapNotNull(memoryLinkMapper::toModel)
    }

    @Transactional
    fun getMemoryPointsByIds(ids: Collection<UUID>): List<MemoryPoint> {
        return memoryPointRepository.findByIdIn(ids).mapNotNull(memoryPointMapper::toModel)
    }

    @Transactional
    fun getMemoryKeysByIds(ids: Collection<UUID>): List<MemoryKey> {
        return memoryKeyRepository.findByIdIn(ids).mapNotNull(memoryKeyMapper::toModel)
    }

    @Transactional
    fun getOrCreateMemoryPoint(value: String): MemoryPoint {
        val entity = memoryPointRepository.findByValue(value)
            ?: memoryPointRepository.save(
                MemoryPointEntity().also {
                    it.value = value
                }
            )
        return memoryPointMapper.toModel(entity)!!
    }

    @Transactional
    fun getOrCreateMemoryKey(keyText: String, embedding: FloatArray, embeddingModelId: Long): MemoryKey {
        val entity = memoryKeyRepository.findByKeyText(keyText)
            ?: memoryKeyRepository.save(
                MemoryKeyEntity().also {
                    it.keyText = keyText
                    it.embedding = embedding
                    it.embeddingModel = embeddingModelRepository.getReferenceById(embeddingModelId)
                }
            )
        return memoryKeyMapper.toModel(entity)!!
    }

    @Transactional(readOnly = true)
    fun findNearestMemoryKeys(embedding: FloatArray, limit: Int): List<MemoryKey> {
        val embeddingStr = embedding.joinToString(",", "[", "]")
        return memoryKeyRepository.findNearest(embeddingStr, limit).map { memoryKeyMapper.toModel(it)!! }
    }

    @Transactional(readOnly = true)
    fun getMemoryLinksByMemoryPoint(memoryPointId: UUID): List<MemoryLink> =
        memoryLinkRepository.findByMemoryPointId(memoryPointId).map { memoryLinkMapper.toModel(it)!! }

    @Transactional(readOnly = true)
    fun getMemoryLinksByMemoryKey(memoryKeyId: UUID): List<MemoryLink> =
        memoryLinkRepository.findByMemoryKeyId(memoryKeyId).map { memoryLinkMapper.toModel(it)!! }

    @Transactional
    fun upsertMemoryLink(memoryKeyId: UUID, memoryPointId: UUID, weight: Double, timestamp: ZonedDateTime) {
        val linkId = MemoryLinkEntityId(memoryKeyId, memoryPointId)
        val existing = memoryLinkRepository.findById(linkId)

        if (existing.isPresent) {
            memoryLinkRepository.updateWeightAndLastUpdatedAt(
                memoryKeyId,
                memoryPointId,
                weight,
                timestamp.toOffsetDateTime()
            )
        } else {
            memoryLinkRepository.save(
                MemoryLinkEntity().apply {
                    this.memoryKeyId = memoryKeyId
                    this.memoryPointId = memoryPointId
                    this.weight = weight
                    this.lastUpdatedAt = timestamp.toOffsetDateTime()
                }
            )
        }
    }
}