package space.davids_digital.kiri.rest.dto

data class ToolDto(
    val fullName: String,
    val description: String?,
    val parameters: ToolParameterValueDto
)

sealed interface ToolParameterValueDto {
    val type: String

    data class ObjectValue(
        val description: String?,
        val properties: Map<String, ToolParameterValueDto>,
        val required: List<String>
    ) : ToolParameterValueDto {
        override val type = "object"
    }

    data class ArrayValue(
        val description: String?,
        val items: ToolParameterValueDto
    ) : ToolParameterValueDto {
        override val type = "array"
    }

    data class StringValue(
        val description: String?,
        val enum: List<String>?
    ) : ToolParameterValueDto {
        override val type = "string"
    }

    data class NumberValue(
        val description: String?
    ) : ToolParameterValueDto {
        override val type = "number"
    }

    data class BooleanValue(
        val description: String?
    ) : ToolParameterValueDto {
        override val type = "boolean"
    }
}

data class ToolExecuteRequest(
    val toolName: String,
    val input: ToolInputDto
)
