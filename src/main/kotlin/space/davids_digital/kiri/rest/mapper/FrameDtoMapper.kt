package space.davids_digital.kiri.rest.mapper

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.Frame
import space.davids_digital.kiri.agent.frame.ToolCallFrame
import space.davids_digital.kiri.llm.LlmToolUse
import space.davids_digital.kiri.llm.LlmToolUseResult
import space.davids_digital.kiri.rest.dto.*
import java.util.Base64

/**
 * Converts internal frame structures into DTOs that are transferred to the admin UI.
 */
@Component
class FrameDtoMapper {
    suspend fun map(frame: Frame): FrameDto = when (frame) {
        is DataFrame -> mapDataFrame(frame)
        is ToolCallFrame -> mapToolCallFrame(frame)
    }

    suspend fun mapDataFrame(frame: DataFrame): DataFrameDto {
        val parts = frame.renderContent().map { part ->
            when (part) {
                is DataFrame.Text -> ContentPartDto.Text(part.text)
                is DataFrame.Image -> ContentPartDto.Image(
                    Base64.getEncoder().encodeToString(part.data),
                    part.type.name
                )
            }
        }
        return DataFrameDto(frame.tag, frame.attributes, parts)
    }

    private fun mapToolCallFrame(frame: ToolCallFrame): ToolCallFrameDto {
        val toolUseDto = mapToolUse(frame.toolUse)
        val resultDto = mapToolResult(frame.resultProvider())
        return ToolCallFrameDto(toolUseDto, resultDto)
    }

    private fun mapToolUse(toolUse: LlmToolUse): ToolUseDto =
        ToolUseDto(toolUse.id, toolUse.name, mapToolInput(toolUse.input))

    private fun mapToolResult(result: LlmToolUseResult): ToolResultDto =
        ToolResultDto(result.toolUseId, result.name, mapToolOutput(result.output))

    private fun mapToolInput(input: LlmToolUse.Input): ToolInputDto = when (input) {
        is LlmToolUse.Input.Text -> ToolInputDto.Text(input.text)
        is LlmToolUse.Input.Number -> ToolInputDto.Number(input.number)
        is LlmToolUse.Input.Boolean -> ToolInputDto.BooleanVal(input.boolean)
        is LlmToolUse.Input.Array -> ToolInputDto.Array(input.items.map { mapToolInput(it) })
        is LlmToolUse.Input.Object -> ToolInputDto.Object(input.items.mapValues { mapToolInput(it.value) })
    }

    private fun mapToolOutput(output: LlmToolUseResult.Output): ToolOutputDto = when (output) {
        is LlmToolUseResult.Output.Text -> ToolOutputDto.Text(output.text)
        is LlmToolUseResult.Output.Image -> ToolOutputDto.Image(
            Base64.getEncoder().encodeToString(output.data),
            output.mediaType.name
        )
    }
}
