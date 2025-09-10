package space.davids_digital.kiri.orm.mapper

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.EmbeddingModel
import space.davids_digital.kiri.orm.entity.EmbeddingModelEntity

@Mapper
interface EmbeddingModelEntityMapper {
    fun toEntity(model: EmbeddingModel?): EmbeddingModelEntity?
    fun toModel(entity: EmbeddingModelEntity?): EmbeddingModel?
}