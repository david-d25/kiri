package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.MemoryKey
import space.davids_digital.kiri.orm.entity.MemoryKeyEntity

@Mapper(componentModel = "spring", uses = [EmbeddingModelEntityMapper::class])
interface MemoryKeyEntityMapper {
    fun toEntity(model: MemoryKey?): MemoryKeyEntity?
    fun toModel(entity: MemoryKeyEntity?): MemoryKey?
}