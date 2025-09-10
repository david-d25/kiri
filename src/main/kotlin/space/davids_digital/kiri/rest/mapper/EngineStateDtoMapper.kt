package space.davids_digital.kiri.rest.mapper

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.EngineState
import space.davids_digital.kiri.rest.dto.EngineStateDto

@Mapper
interface EngineStateDtoMapper {
    fun toModel(dto: EngineStateDto): EngineState
    fun toDto(model: EngineState): EngineStateDto
}