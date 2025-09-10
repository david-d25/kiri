package space.davids_digital.kiri.orm.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.MemoryLink
import space.davids_digital.kiri.orm.entity.MemoryLinkEntity

@Mapper(uses = [DateTimeMapper::class])
interface MemoryLinkEntityMapper {
    @Mapping(target = "memoryKey", ignore = true)
    @Mapping(target = "memoryPoint", ignore = true)
    fun toEntity(model: MemoryLink?): MemoryLinkEntity?

    fun toModel(entity: MemoryLinkEntity?): MemoryLink?
}