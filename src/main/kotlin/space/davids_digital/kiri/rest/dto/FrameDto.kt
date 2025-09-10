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
    val type: String

    data class Text(val text: String) : ContentPartDto {
        override val type = "text"
    }
    data class Image(val base64: String, val imageType: String) : ContentPartDto {
        override val type = "image"
    }
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
    val type: String

    data class Text(val text: String) : ToolInputDto {
        override val type = "text"
    }
    data class Number(val number: Double) : ToolInputDto {
        override val type = "number"
    }
    data class BooleanVal(val boolean: Boolean) : ToolInputDto {
        override val type = "boolean"
    }
    data class Array(val items: List<ToolInputDto>) : ToolInputDto {
        override val type = "array"
    }
    data class Object(val items: Map<String, ToolInputDto>) : ToolInputDto {
        override val type = "object"
    }
}

sealed interface ToolOutputDto {
    val type: String

    data class Text(val text: String) : ToolOutputDto {
        override val type = "text"
    }
    data class Image(val base64: String, val imageType: String) : ToolOutputDto {
        override val type = "image"
    }
}
