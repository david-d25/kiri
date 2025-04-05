package space.davids_digital.kiri.orm.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.EmbeddingModelEntity

@Repository
interface EmbeddingModelRepository : JpaRepository<EmbeddingModelEntity, Long> {
    fun findByNameAndVendorAndDimensionality(name: String, vendor: String, dimensionality: Int): EmbeddingModelEntity?
}