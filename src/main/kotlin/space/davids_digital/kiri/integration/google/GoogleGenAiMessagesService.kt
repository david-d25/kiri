package space.davids_digital.kiri.integration.google

import com.google.genai.Client
import com.google.genai.types.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.llm.*
import space.davids_digital.kiri.llm.LlmMessageRequest.Message
import space.davids_digital.kiri.llm.LlmMessageRequest.Tools.Function.ParameterValue
import space.davids_digital.kiri.llm.dsl.LlmToolUseInputArrayBuilder
import space.davids_digital.kiri.llm.dsl.LlmToolUseInputBuilder
import space.davids_digital.kiri.llm.dsl.LlmToolUseInputObjectBuilder
import space.davids_digital.kiri.llm.dsl.llmMessageResponse
import space.davids_digital.kiri.service.LlmService
import kotlin.jvm.optionals.getOrNull

@Service
class GoogleGenAiMessagesService(settings: Settings) : LlmService<String> {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val client = Client.builder().apiKey(settings.integration.google.genAi.apiKey).build()

    override suspend fun request(request: LlmMessageRequest): LlmMessageResponse {
        val content = buildContent(request)
        val config = buildConfig(request)
        val response = client.models.generateContent(request.model, content, config)
        return parseResponse(response)
    }

    private fun buildContent(request: LlmMessageRequest) = request.messages.map { message ->
        val builder = Content.builder()
        when (message.role) {
            Message.Role.USER -> builder.role("user")
            Message.Role.ASSISTANT -> builder.role("model")
        }
        builder.parts(
            message.content.map { contentItem ->
                when (contentItem) {
                    is Message.ContentItem.Text -> Part.fromText(contentItem.text)
                    is Message.ContentItem.Image -> Part.fromBytes(contentItem.data, contentItem.mediaType.toMimeType())
                    is Message.ContentItem.ToolUse -> Part.builder().functionCall(
                        FunctionCall.builder()
                            .id(contentItem.toolUse.id)
                            .name(contentItem.toolUse.name)
                            .args(buildContentToolUseInputToMap(contentItem.toolUse.input))
                            .build()
                    ).build()
                    is Message.ContentItem.ToolResult -> Part.builder().functionResponse(
                        FunctionResponse.builder()
                            .id(contentItem.toolResult.toolUseId)
                            .name(contentItem.toolResult.name)
                            .response(buildContentToolResultOutput(contentItem.toolResult.output))
                            .build()
                    ).build()
                }
            }
        )
        builder.build()
    }

    private fun buildContentToolUseInputToMap(input: LlmToolUse.Input): Map<String, Any> {
        return buildMap {
            when (input) {
                is LlmToolUse.Input.Text -> put("text", input.text)
                is LlmToolUse.Input.Number -> put("number", input.number)
                is LlmToolUse.Input.Boolean -> put("boolean", input.boolean)
                is LlmToolUse.Input.Array -> put("array", input.items.map(::buildContentToolUseInput))
                is LlmToolUse.Input.Object -> input.items.forEach {
                    put(it.key, buildContentToolUseInput(it.value))
                }
            }
        }
    }

    private fun buildContentToolUseInput(input: LlmToolUse.Input): Any {
        return when (input) {
            is LlmToolUse.Input.Text -> input.text
            is LlmToolUse.Input.Number -> input.number
            is LlmToolUse.Input.Boolean -> input.boolean
            is LlmToolUse.Input.Array -> input.items.map(::buildContentToolUseInput)
            is LlmToolUse.Input.Object -> buildContentToolUseInputToMap(input)
        }
    }

    private fun buildContentToolResultOutput(output: LlmToolUseResult.Output): Map<String, Any> {
        return when (output) {
            is LlmToolUseResult.Output.Text -> mapOf("output" to output.text)
            is LlmToolUseResult.Output.Image -> mapOf("output" to output.data)
        }
    }

    private fun buildConfig(request: LlmMessageRequest): GenerateContentConfig {
        val mode = when(request.tools.choice) {
            LlmMessageRequest.Tools.ToolChoice.AUTO -> "AUTO"
            LlmMessageRequest.Tools.ToolChoice.NONE -> "NONE"
            LlmMessageRequest.Tools.ToolChoice.REQUIRED -> "ANY"
        }
        return GenerateContentConfig.builder()
            .temperature(request.temperature.toFloat())
            .systemInstruction(Content.fromParts(Part.fromText(request.system)))
            .maxOutputTokens(request.maxOutputTokens.toInt())
            .tools(buildTools(request.tools))
            .toolConfig(
                ToolConfig.builder().functionCallingConfig(
                    FunctionCallingConfig.builder().mode(mode).build() // TODO add 'allowParallelUse' when available
                ).build()
            )
            .build()
    }

    private fun buildTools(tool: LlmMessageRequest.Tools): List<Tool> {
        return listOf(
            Tool.builder().functionDeclarations(
                tool.functions.map { function ->
                    val builder = FunctionDeclaration.builder()
                        .name(function.name)
                        .description(function.description)
                    if (function.parameters != null && function.parameters.properties.isNotEmpty()) {
                        builder.parameters(buildFunctionParametersSchema(function.parameters))
                    }
                    builder.build()
                }
            ).build()
        )
    }

    private fun buildFunctionParametersSchema(parameters: ParameterValue.ObjectValue): Schema {
        val builder = Schema.builder()
            .type("object")
            .properties(parameters.properties.mapValues { parameterToSchema(it.value) })

        if (parameters.required.isNotEmpty()) {
            builder.required(parameters.required)
        }

        return builder.build()
    }

    private fun parameterToSchema(value: ParameterValue): Schema {
        return when (value) {
            is ParameterValue.ObjectValue -> {
                val schema = Schema.builder()
                    .type("object")
                    .properties(value.properties.mapValues { parameterToSchema(it.value) })
                if (value.required.isNotEmpty()) {
                    schema.required(value.required)
                }
                if (value.description != null) {
                    schema.description(value.description)
                }
                return schema.build()
            }
            is ParameterValue.StringValue -> {
                val schema = Schema.builder().type("string")
                if (value.description != null) {
                    schema.description(value.description)
                }
                if (value.enum?.isNotEmpty() == true) {
                    schema.enum_(value.enum)
                }
                return schema.build()
            }
            is ParameterValue.NumberValue -> {
                val schema = Schema.builder().type("number")
                if (value.description != null) {
                    schema.description(value.description)
                }
                return schema.build()
            }
            is ParameterValue.BooleanValue -> {
                val schema = Schema.builder().type("boolean")
                if (value.description != null) {
                    schema.description(value.description)
                }
                return schema.build()
            }
        }
    }

    private fun parseResponse(response: GenerateContentResponse) = llmMessageResponse {
        val finishReasonString = response.candidates().getOrNull()?.first()?.finishReason()?.getOrNull()
        id = response.responseId().orElse("")
        stopReason = when (finishReasonString) {
            "STOP" -> LlmMessageResponse.StopReason.END_TURN
            "MAX_TOKENS" -> LlmMessageResponse.StopReason.MAX_TOKENS
            else -> LlmMessageResponse.StopReason.UNKNOWN
        }
        content {
            response.parts()?.forEach { part ->
                part.text().ifPresent(::text)
                part.functionCall().ifPresent { functionCall ->
                    toolUse {
                        id = functionCall.id().orElse("<unknown_id>")
                        name = functionCall.name().orElse("<unknown_name>")
                        input {
                            functionCall.args().ifPresent { parseToolUseInput(it) }
                        }
                    }
                }
            }
        }
        usage {
            response.usageMetadata().ifPresent {
                inputTokens = it.promptTokenCount().orElse(-1).toLong()
                if (it.thoughtsTokenCount().isEmpty && it.candidatesTokenCount().isEmpty) {
                    outputTokens = -1
                } else {
                    outputTokens = (it.thoughtsTokenCount().orElse(0) + it.candidatesTokenCount().orElse(0)).toLong()
                }
            }
        }
    }

    private fun LlmToolUseInputBuilder.parseToolUseInput(input: Any) {
        when (input) {
            is String -> text(input)
            is Number -> number(input.toDouble())
            is Boolean -> boolean(input)
            is Collection<*> -> array {
                input.forEach { parseToolUseInput(it) }
            }
            is Map<*, *> -> objectValue {
                input.forEach { parseToolUseInput(it.key, it.value) }
            }
            else -> throw IllegalArgumentException("Unsupported input type: ${input::class}")
        }
    }

    private fun LlmToolUseInputArrayBuilder.parseToolUseInput(input: Any?) {
        when (input) {
            is String -> text(input)
            is Number -> number(input.toDouble())
            is Boolean -> boolean(input)
            is Collection<*> -> array {
                input.forEach { parseToolUseInput(it) }
            }
            is Map<*, *> -> objectValue {
                input.forEach { parseToolUseInput(it.key, it.value) }
            }
            else -> if (input != null) {
                throw IllegalArgumentException("Unsupported input type: ${input::class}")
            }
        }
    }

    private fun LlmToolUseInputObjectBuilder.parseToolUseInput(key: Any?, value: Any?) {
        if (key == null) {
            log.error("Key is null")
            return
        }
        val keyString = key.toString()
        when (value) {
            is String -> text(keyString, value)
            is Number -> number(keyString, value.toDouble())
            is Boolean -> boolean(keyString, value)
            is Collection<*> -> array(keyString) {
                value.forEach { parseToolUseInput(it) }
            }
            is Map<*, *> -> objectValue(keyString) {
                value.forEach { parseToolUseInput(it.key, it.value) }
            }
            else -> if (value != null) {
                throw IllegalArgumentException("Unsupported input type: ${value::class}")
            }
        }
    }

    private fun LlmImageType.toMimeType() = when (this) {
        LlmImageType.JPEG -> "image/jpeg"
        LlmImageType.PNG -> "image/png"
        LlmImageType.GIF -> "image/gif"
        LlmImageType.WEBP -> "image/webp"
    }
}