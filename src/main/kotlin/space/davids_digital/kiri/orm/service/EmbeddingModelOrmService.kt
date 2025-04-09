package space.davids_digital.kiri.orm.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.EmbeddingModel
import space.davids_digital.kiri.orm.entity.EmbeddingModelEntity
import space.davids_digital.kiri.orm.mapping.EmbeddingModelEntityMapper
import space.davids_digital.kiri.orm.repository.EmbeddingModelRepository

@Service
class EmbeddingModelOrmService(
    private val repo: EmbeddingModelRepository,
    private val mapper: EmbeddingModelEntityMapper,
) {
    @Transactional
    fun getOrCreate(name: String, vendor: String, dimensions: Int): EmbeddingModel {
        val entity = repo.findByNameAndVendorAndDimensionality(name, vendor, dimensions).orElseGet {
            repo.save(
                EmbeddingModelEntity().also {
                    it.name = name
                    it.vendor = vendor
                    it.dimensionality = dimensions
                }
            )
        }
        return mapper.toModel(entity)!!
    }
}