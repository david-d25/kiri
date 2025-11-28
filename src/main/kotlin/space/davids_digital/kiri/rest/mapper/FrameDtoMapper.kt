package space.davids_digital.kiri.rest.mapper

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.Frame
import space.davids_digital.kiri.agent.frame.ToolCallFrame
import space.davids_digital.kiri.llm.ChatCompletionToolUse
import space.davids_digital.kiri.llm.ChatCompletionToolUseResult
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

    private fun mapToolUse(toolUse: ChatCompletionToolUse): ToolUseDto =
        ToolUseDto(toolUse.id, toolUse.name, mapToolInput(toolUse.input))

    private fun mapToolResult(result: ChatCompletionToolUseResult): ToolResultDto =
        ToolResultDto(result.toolUseId, result.name, mapToolOutput(result.output))

    private fun mapToolInput(input: ChatCompletionToolUse.Input): ToolInputDto = when (input) {
        is ChatCompletionToolUse.Input.Text -> ToolInputDto.Text(input.text)
        is ChatCompletionToolUse.Input.Number -> ToolInputDto.Number(input.number)
        is ChatCompletionToolUse.Input.Boolean -> ToolInputDto.BooleanVal(input.boolean)
        is ChatCompletionToolUse.Input.Array -> ToolInputDto.Array(input.items.map { mapToolInput(it) })
        is ChatCompletionToolUse.Input.Object -> ToolInputDto.Object(input.items.mapValues { mapToolInput(it.value) })
    }

    private fun mapToolOutput(output: ChatCompletionToolUseResult.Output): ToolOutputDto = when (output) {
        is ChatCompletionToolUseResult.Output.Text -> ToolOutputDto.Text(output.text)
        is ChatCompletionToolUseResult.Output.Image -> ToolOutputDto.Image(
            Base64.getEncoder().encodeToString(output.data),
            output.mediaType.name
        )
    }
}
