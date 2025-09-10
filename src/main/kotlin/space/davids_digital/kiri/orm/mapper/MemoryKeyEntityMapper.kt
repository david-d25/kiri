package space.davids_digital.kiri.orm.mapper

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.MemoryKey
import space.davids_digital.kiri.orm.entity.MemoryKeyEntity

@Mapper(uses = [EmbeddingModelEntityMapper::class])
interface MemoryKeyEntityMapper {
    fun toEntity(model: MemoryKey?): MemoryKeyEntity?
    fun toModel(entity: MemoryKeyEntity?): MemoryKey?
}