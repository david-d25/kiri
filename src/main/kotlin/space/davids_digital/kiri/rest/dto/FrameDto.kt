package space.davids_digital.kiri.rest.dto

/**
 * Base DTO for data sent to the admin front‑end representing a frame inside the agent buffer.
 * Concrete implementations are {@link DataFrameDto} and {@link ToolCallFrameDto}.
 */
sealed interface FrameDto {
    val type: String
}

data class DataFrameDto(
    val tag: String,
    val attributes: Map<String, String>,
    val content: List<ContentPartDto>,
) : FrameDto {
    override val type: String = "data"
}

sealed interface ContentPartDto {
    data class Text(val text: String) : ContentPartDto
    data class Image(val base64: String, val imageType: String) : ContentPartDto
}

data class ToolCallFrameDto(
    val toolUse: ToolUseDto,
    val result: ToolResultDto,
) : FrameDto {
    override val type: String = "toolCall"
}

data class ToolUseDto(
    val id: String,
    val name: String,
    val input: ToolInputDto,
)

data class ToolResultDto(
    val toolUseId: String,
    val name: String,
    val output: ToolOutputDto,
)

sealed interface ToolInputDto {
    data class Text(val text: String) : ToolInputDto
    data class Number(val number: Double) : ToolInputDto
    data class BooleanVal(val boolean: Boolean) : ToolInputDto
    data class Array(val items: List<ToolInputDto>) : ToolInputDto
    data class Object(val items: Map<String, ToolInputDto>) : ToolInputDto
}

sealed interface ToolOutputDto {
    data class Text(val text: String) : ToolOutputDto
    data class Image(val base64: String, val imageType: String) : ToolOutputDto
}
