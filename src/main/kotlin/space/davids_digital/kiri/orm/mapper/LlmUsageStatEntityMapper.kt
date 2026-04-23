package space.davids_digital.kiri.orm.mapper

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.LlmUsageStat
import space.davids_digital.kiri.orm.entity.LlmUsageStatEntity

@Mapper(uses = [DateTimeMapper::class])
interface LlmUsageStatEntityMapper {
    fun toEntity(model: LlmUsageStat?): LlmUsageStatEntity?
    fun toModel(entity: LlmUsageStatEntity?): LlmUsageStat?
}
