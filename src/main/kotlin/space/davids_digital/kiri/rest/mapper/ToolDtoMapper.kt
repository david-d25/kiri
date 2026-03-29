package space.davids_digital.kiri.rest.mapper

import org.springframework.stereotype.Component
import space.davids_digital.kiri.llm.ChatCompletionRequest.Tools.Function.ParameterValue
import space.davids_digital.kiri.llm.ChatCompletionToolUse
import space.davids_digital.kiri.rest.dto.ToolInputDto
import space.davids_digital.kiri.rest.dto.ToolParameterValueDto

@Component
class ToolDtoMapper {
    fun mapParameterValue(pv: ParameterValue): ToolParameterValueDto = when (pv) {
        is ParameterValue.ObjectValue -> ToolParameterValueDto.ObjectValue(
            description = pv.description,
            properties = pv.properties.mapValues { mapParameterValue(it.value) },
            required = pv.required
        )
        is ParameterValue.ArrayValue -> ToolParameterValueDto.ArrayValue(
            description = pv.description,
            items = mapParameterValue(pv.items)
        )
        is ParameterValue.StringValue -> ToolParameterValueDto.StringValue(
            description = pv.description,
            enum = pv.enum
        )
        is ParameterValue.NumberValue -> ToolParameterValueDto.NumberValue(
            description = pv.description
        )
        is ParameterValue.BooleanValue -> ToolParameterValueDto.BooleanValue(
            description = pv.description
        )
    }

    fun mapToolInput(dto: ToolInputDto): ChatCompletionToolUse.Input = when (dto) {
        is ToolInputDto.Text -> ChatCompletionToolUse.Input.Text(dto.text)
        is ToolInputDto.Number -> ChatCompletionToolUse.Input.Number(dto.number)
        is ToolInputDto.BooleanVal -> ChatCompletionToolUse.Input.Boolean(dto.boolean)
        is ToolInputDto.Array -> ChatCompletionToolUse.Input.Array(dto.items.map { mapToolInput(it) })
        is ToolInputDto.Object -> ChatCompletionToolUse.Input.Object(dto.items.mapValues { mapToolInput(it.value) })
    }
}
