package space.davids_digital.kiri.integration.anthropic

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.JsonArray
import com.anthropic.core.JsonBoolean
import com.anthropic.core.JsonMissing
import com.anthropic.core.JsonNull
import com.anthropic.core.JsonNumber
import com.anthropic.core.JsonObject
import com.anthropic.core.JsonString
import com.anthropic.core.JsonValue
import com.anthropic.models.Base64ImageSource
import com.anthropic.models.ContentBlockParam
import com.anthropic.models.ImageBlockParam
import com.anthropic.models.Message.StopReason.Companion.END_TURN
import com.anthropic.models.Message.StopReason.Companion.MAX_TOKENS
import com.anthropic.models.Message.StopReason.Companion.STOP_SEQUENCE
import com.anthropic.models.Message.StopReason.Companion.TOOL_USE
import com.anthropic.models.MessageCreateParams
import com.anthropic.models.MessageParam
import com.anthropic.models.Model
import com.anthropic.models.TextBlockParam
import com.anthropic.models.Tool
import com.anthropic.models.ToolChoice
import com.anthropic.models.ToolChoiceAny
import com.anthropic.models.ToolChoiceAuto
import com.anthropic.models.ToolChoiceNone
import com.anthropic.models.ToolResultBlockParam
import com.anthropic.models.ToolUnion
import com.anthropic.models.ToolUseBlockParam
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.llm.LlmImageType
import space.davids_digital.kiri.llm.LlmMessageRequest
import space.davids_digital.kiri.llm.LlmMessageRequest.Message
import space.davids_digital.kiri.llm.LlmMessageRequest.Tools.Function.ParameterValue
import space.davids_digital.kiri.llm.LlmMessageResponse
import space.davids_digital.kiri.llm.LlmToolUse
import space.davids_digital.kiri.llm.LlmToolUseResult
import space.davids_digital.kiri.llm.dsl.LlmToolUseInputArrayBuilder
import space.davids_digital.kiri.llm.dsl.LlmToolUseInputBuilder
import space.davids_digital.kiri.llm.dsl.LlmToolUseInputObjectBuilder
import space.davids_digital.kiri.llm.dsl.llmMessageResponse
import space.davids_digital.kiri.service.LlmService
import java.util.Base64
import kotlin.jvm.optionals.getOrNull

@Service
class AnthropicMessagesService(settings: Settings) : LlmService<Model> {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val anthropic = AnthropicOkHttpClient.builder().apiKey(settings.integration.anthropic.apiKey).build()

    override suspend fun request(request: LlmMessageRequest): LlmMessageResponse {
        val params = buildParams(request)
        val response = anthropic.messages().create(params)
        return parseResponse(response)
    }

    private fun buildParams(request: LlmMessageRequest) = MessageCreateParams.builder().apply {
        model(request.model)
        system(request.system)
        maxTokens(request.maxOutputTokens)
        temperature(request.temperature)
        messages(buildMessages(request.messages))
        tools(buildTools(request.tools))
        toolChoice(
            when (request.tools.choice) {
                LlmMessageRequest.Tools.ToolChoice.AUTO -> ToolChoice.ofAuto(
                    ToolChoiceAuto.builder()
                        .disableParallelToolUse(!request.tools.allowParallelUse)
                        .build()
                )
                LlmMessageRequest.Tools.ToolChoice.REQUIRED -> ToolChoice.ofAny(
                    ToolChoiceAny.builder()
                        .disableParallelToolUse(!request.tools.allowParallelUse)
                        .build()
                )
                LlmMessageRequest.Tools.ToolChoice.NONE -> ToolChoice.ofNone(ToolChoiceNone.builder().build())
            }
        )
    }.build()

    private fun parseResponse(response: com.anthropic.models.Message) = llmMessageResponse {
        id = response.id()
        stopReason = when (response.stopReason().getOrNull()) {
            END_TURN -> LlmMessageResponse.StopReason.END_TURN
            MAX_TOKENS -> LlmMessageResponse.StopReason.MAX_TOKENS
            STOP_SEQUENCE -> LlmMessageResponse.StopReason.STOP_SEQUENCE
            TOOL_USE -> LlmMessageResponse.StopReason.TOOL_USE
            null -> null
            else -> LlmMessageResponse.StopReason.UNKNOWN
        }
        content {
            response.content().forEach { contentBlock ->
                if (contentBlock.isText()) {
                    text(contentBlock.asText().text())
                } else if (contentBlock.isToolUse()) {
                    val toolUse = contentBlock.asToolUse()
                    toolUse {
                        id = toolUse.id()
                        name = toolUse.name()
                        input {
                            parseToolUseInput(toolUse._input())
                        }
                    }
                } else {
                    log.warn("Unsupported content block type: {}", contentBlock)
                }
            }
        }
        usage {
            inputTokens = response.usage().inputTokens()
            outputTokens = response.usage().outputTokens()
        }
    }

    private fun LlmToolUseInputBuilder.parseToolUseInput(input: JsonValue) {
        when (input) {
            is JsonBoolean -> boolean(input.value)
            is JsonNumber -> number(input.value.toDouble())
            is JsonString -> text(input.value)
            is JsonArray -> array {
                input.values.forEach { parseToolUseInput(it) }
            }
            is JsonObject -> objectValue {
                input.values.entries.forEach { (key, value) -> parseToolUseInput(key, value) }
            }
            is JsonMissing, is JsonNull -> objectValue { }
        }
    }

    private fun LlmToolUseInputArrayBuilder.parseToolUseInput(input: JsonValue) {
        when (input) {
            is JsonBoolean -> boolean(input.value)
            is JsonNumber -> number(input.value.toDouble())
            is JsonString -> text(input.value)
            is JsonArray -> array {
                input.values.forEach { parseToolUseInput(it) }
            }
            is JsonObject -> objectValue {
                input.values.entries.forEach { (key, value) -> parseToolUseInput(key, value) }
            }
            is JsonMissing, is JsonNull -> objectValue { }
        }
    }

    private fun LlmToolUseInputObjectBuilder.parseToolUseInput(key: String, input: JsonValue) {
        when (input) {
            is JsonBoolean -> boolean(key, input.value)
            is JsonNumber -> number(key, input.value.toDouble())
            is JsonString -> text(key, input.value)
            is JsonArray -> array(key) {
                input.values.forEach { parseToolUseInput(it) }
            }
            is JsonObject -> objectValue(key) {
                input.values.entries.forEach { (key, value) -> parseToolUseInput(key, value) }
            }
            is JsonMissing, is JsonNull -> null
        }
    }

    private fun buildMessages(messages: List<Message>) = messages.map { message ->
        val builder = MessageParam.builder()
        when (message.role) {
            Message.Role.USER -> builder.role(MessageParam.Role.USER)
            Message.Role.ASSISTANT -> builder.role(MessageParam.Role.ASSISTANT)
        }
        builder.content(MessageParam.Content.ofBlockParams(
            message.content.map { contentItem ->
                when (contentItem) {
                    is Message.ContentItem.Text -> ContentBlockParam.ofText(
                        TextBlockParam.builder().text(contentItem.text).build()
                    )
                    is Message.ContentItem.Image -> ContentBlockParam.ofImage(
                        buildImageBlock(contentItem)
                    )
                    is Message.ContentItem.ToolUse -> ContentBlockParam.ofToolUse(
                        ToolUseBlockParam.builder()
                            .id(contentItem.toolUse.id)
                            .name(contentItem.toolUse.name)
                            .input(JsonValue.fromJsonNode(toolUseInputToJson(contentItem.toolUse.input)))
                            .build()
                    )
                    is Message.ContentItem.ToolResult -> ContentBlockParam.ofToolResult(
                        ToolResultBlockParam.builder()
                            .toolUseId(contentItem.toolResult.toolUseId)
                            .contentOfBlocks(toolUseResultOutputToContentBlockList(contentItem.toolResult.output))
                            .build()
                    )
                }
            }
        ))
        builder.build()
    }

    private fun buildTools(tools: LlmMessageRequest.Tools) = tools.functions.map { function ->
        ToolUnion.ofTool(
            Tool.builder()
                .name(function.name)
                .inputSchema(buildInputSchema(function.parameters))
                .also {
                    if (function.description != null) {
                        it.description(function.description)
                    }
                }
                .build()
        )
    }

    private fun buildInputSchema(parameters: ParameterValue.ObjectValue?): Tool.InputSchema {
        if (parameters == null) {
            return Tool.InputSchema.builder().type(JsonValue.from("object")).build()
        }
        val builder = Tool.InputSchema.builder()
            .type(JsonValue.from("object"))
            .properties(JsonValue.from(parameters.properties.mapValues { parameterToJson(it.value) }))

        if (parameters.required.isNotEmpty()) {
            builder.additionalProperties(mapOf(
                "required" to JsonValue.from(parameters.required),
            ))
        }

        return builder.build()
    }

    private fun toolUseResultOutputToContentBlockList(
        toolUseResult: LlmToolUseResult.Output
    ): List<ToolResultBlockParam.Content.Block> {
        return listOf(
            when(toolUseResult) {
                is LlmToolUseResult.Output.Text -> ToolResultBlockParam.Content.Block.ofTextBlockParam(
                    TextBlockParam.builder().text(toolUseResult.text).build()
                )
                is LlmToolUseResult.Output.Image -> ToolResultBlockParam.Content.Block.ofImageBlockParam(
                    ImageBlockParam.builder()
                        .source(
                            Base64ImageSource.builder()
                                .data(Base64.getEncoder().encodeToString(toolUseResult.data))
                                .mediaType(mapImageMediaType(toolUseResult.mediaType))
                                .build()
                        )
                        .build()
                )
            }
        )
    }

    private fun toolUseInputToJson(toolUse: LlmToolUse.Input): JsonNode {
        val factory = JsonNodeFactory.instance
        return when (toolUse) {
            is LlmToolUse.Input.Text -> factory.textNode(toolUse.text)
            is LlmToolUse.Input.Number -> factory.numberNode(toolUse.number)
            is LlmToolUse.Input.Boolean -> factory.booleanNode(toolUse.boolean)
            is LlmToolUse.Input.Array -> factory.arrayNode().apply {
                toolUse.items.forEach { add(toolUseInputToJson(it)) }
            }
            is LlmToolUse.Input.Object -> factory.objectNode().apply {
                toolUse.items.forEach { (key, value) ->
                    set<JsonNode>(key, toolUseInputToJson(value))
                }
            }
        }
    }

    private fun parameterToJson(value: ParameterValue): JsonValue {
        return when (value) {
            is ParameterValue.ObjectValue -> {
                val map = mutableMapOf(
                    "type" to JsonValue.from("object"),
                    "properties" to JsonValue.from(value.properties.mapValues { parameterToJson(it.value) }),
                )
                if (value.required.isNotEmpty()) {
                    map["required"] = JsonValue.from(value.required)
                }
                if (value.description != null) {
                    map["description"] = JsonValue.from(value.description)
                }
                JsonValue.from(map)
            }
            is ParameterValue.StringValue -> {
                val map = mutableMapOf("type" to JsonValue.from("string"))
                if (value.description != null) {
                    map["description"] = JsonValue.from(value.description)
                }
                if (value.enum?.isNotEmpty() == true) {
                    map["enum"] = JsonValue.from(value.enum)
                }
                JsonValue.from(map)
            }
            is ParameterValue.NumberValue -> {
                val map = mutableMapOf("type" to JsonValue.from("number"))
                if (value.description != null) {
                    map["description"] = JsonValue.from(value.description)
                }
                JsonValue.from(map)
            }
            is ParameterValue.BooleanValue -> {
                val map = mutableMapOf("type" to JsonValue.from("boolean"))
                if (value.description != null) {
                    map["description"] = JsonValue.from(value.description)
                }
                JsonValue.from(map)
            }
        }
    }

    private fun buildImageBlock(imageContentItem: Message.ContentItem.Image) = ImageBlockParam.builder().apply {
        source(
            Base64ImageSource.builder()
                .data(Base64.getEncoder().encodeToString(imageContentItem.data))
                .mediaType(mapImageMediaType(imageContentItem.mediaType))
                .build()
        )
    }.build()

    private fun mapImageMediaType(mediaType: LlmImageType) = when (mediaType) {
        LlmImageType.JPEG -> Base64ImageSource.MediaType.IMAGE_JPEG
        LlmImageType.PNG -> Base64ImageSource.MediaType.IMAGE_PNG
        LlmImageType.GIF -> Base64ImageSource.MediaType.IMAGE_GIF
        LlmImageType.WEBP -> Base64ImageSource.MediaType.IMAGE_WEBP
    }
}