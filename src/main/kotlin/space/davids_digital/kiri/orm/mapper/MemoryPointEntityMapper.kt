package space.davids_digital.kiri.orm.mapper

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.MemoryPoint
import space.davids_digital.kiri.orm.entity.MemoryPointEntity

@Mapper(uses = [DateTimeMapper::class])
interface MemoryPointEntityMapper {
    fun toEntity(model: MemoryPoint?): MemoryPointEntity?
    fun toModel(entity: MemoryPointEntity?): MemoryPoint?
}