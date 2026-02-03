package space.davids_digital.kiri.rest.mapper

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.ChatCompletionModel
import space.davids_digital.kiri.rest.dto.ChatCompletionModelDto

@Mapper
abstract class ChatCompletionModelDtoMapper {
    abstract fun toModel(dto: ChatCompletionModelDto): ChatCompletionModel
    abstract fun toModel(dto: ChatCompletionModelDto.Features): ChatCompletionModel.Features
    abstract fun toModel(dto: ChatCompletionModelDto.ReasoningType): ChatCompletionModel.ReasoningType

    abstract fun toDto(model: ChatCompletionModel): ChatCompletionModelDto
    abstract fun toDto(model: ChatCompletionModel.Features): ChatCompletionModelDto.Features
    abstract fun toDto(model: ChatCompletionModel.ReasoningType): ChatCompletionModelDto.ReasoningType
}