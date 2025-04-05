package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.EmbeddingModel
import space.davids_digital.kiri.orm.entity.EmbeddingModelEntity

@Mapper(componentModel = "spring")
interface EmbeddingModelEntityMapper {
    fun toEntity(model: EmbeddingModel?): EmbeddingModelEntity?
    fun toModel(entity: EmbeddingModelEntity?): EmbeddingModel?
}