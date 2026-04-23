package space.davids_digital.kiri.integration.google

import com.google.genai.types.*
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import space.davids_digital.kiri.aop.EvictCacheOnException
import space.davids_digital.kiri.llm.*
import space.davids_digital.kiri.llm.ChatCompletionRequest.Message
import space.davids_digital.kiri.llm.ChatCompletionRequest.Tools.Function.ParameterValue
import space.davids_digital.kiri.llm.dsl.ChatCompletionToolUseInputObjectBuilder
import space.davids_digital.kiri.llm.dsl.GenericJsonInputBuilder
import space.davids_digital.kiri.llm.dsl.chatCompletionResponse
import space.davids_digital.kiri.model.ChatCompletionModel
import space.davids_digital.kiri.model.ExternalServiceGatewayStatus
import space.davids_digital.kiri.orm.service.LlmUsageStatOrmService
import space.davids_digital.kiri.service.ChatCompletionService
import kotlin.jvm.optionals.getOrNull

@Service
class GoogleGenAiChatCompletionService(
    private val clientHolder: GoogleGenAiClientHolder,
    private val usageStats: LlmUsageStatOrmService,
) : ChatCompletionService {
    override val serviceHandle = "google-genai-chat"

    private val log = LoggerFactory.getLogger(this::class.java)

    @EvictCacheOnException(cacheNames = ["GoogleGenAiChatCompletionService#getStatus"])
    override suspend fun request(request: ChatCompletionRequest): ChatCompletionResponse {
        val client = clientHolder.requireClient()
        val content = buildContent(request)
        val config = buildConfig(request)
        val (provider, model) = request.modelHandle.split("/", limit = 2)
        val sdkModel = "models/$model"
        if (provider != serviceHandle) {
            throw IllegalArgumentException("Unsupported model provider: $provider")
        }
        if (!isSupportedModelId(sdkModel)) {
            throw IllegalArgumentException("Unsupported model: $sdkModel")
        }
        val startedAt = System.nanoTime()
        val response = client.models.generateContent(sdkModel, content, config)
        val parsed = parseResponse(response)
        val durationMs = (System.nanoTime() - startedAt) / 1_000_000
        usageStats.record(
            provider = serviceHandle,
            model = model,
            inputTokens = parsed.usage.inputTokens,
            outputTokens = parsed.usage.outputTokens,
            cacheReadInputTokens = parsed.usage.cacheReadInputTokens,
            cacheCreationInputTokens = parsed.usage.cacheCreationInputTokens,
            durationMs = durationMs,
        )
        return parsed
    }

    @Cacheable(
        value = ["GoogleGenAiChatCompletionService#getStatus"],
        unless = "#result != T(space.davids_digital.nod3r.model.ExternalServiceGatewayStatus).READY",
        cacheManager = "oneHour"
    )
    override suspend fun getStatus(): ExternalServiceGatewayStatus {
        val client = clientHolder.getClient()
        return if (client != null) {
            try {
                client.models.list(ListModelsConfig.builder().pageSize(1).build())
                ExternalServiceGatewayStatus.READY
            } catch (e: Exception) {
                log.error("Error checking Google GenAI service status", e)
                ExternalServiceGatewayStatus.ERROR
            }
        } else {
            ExternalServiceGatewayStatus.DISABLED
        }
    }

    override suspend fun getModels(): List<ChatCompletionModel> {
        val client = clientHolder.getClient() ?: return emptyList()
        val model = client.models.list(ListModelsConfig.builder().pageSize(100).build())
        return model.filter { it.name().isPresent && isSupportedModelId(it.name().get()) }.map {
            ChatCompletionModel(
                handle = serviceHandle + "/" + it.name().get().removePrefix("models/"),
                features = ChatCompletionModel.Features()
            )
        }
    }

    private fun isSupportedModelId(modelId: String): Boolean {
        return modelId.matches(Regex("models/gemini(-2.\\d)?(-pro|-flash)(-lite)?(-latest)?"))
    }

    private fun buildContent(request: ChatCompletionRequest) = request.messages.map { message ->
        val builder = Content.builder()
        when (message.role) {
            Message.Role.USER -> builder.role("user")
            Message.Role.ASSISTANT -> builder.role("model")
        }
        builder.parts(
            message.content.mapNotNull { contentItem ->
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
                    is Message.ContentItem.Reasoning -> {
                        log.warn("Reasoning content item is not supported by current integration, skipping")
                        null
                    }
                    is Message.ContentItem.RedactedReasoning -> {
                        throw IllegalArgumentException(
                            "RedactedReasoning content item is not supported by Google GenAI integration"
                        )
                    }
                    else -> error("This content item type is not supported")
                }
            }
        )
        builder.build()
    }

    private fun buildContentToolUseInputToMap(input: ChatCompletionToolUse.Input): Map<String, Any> {
        return buildMap {
            when (input) {
                is ChatCompletionToolUse.Input.Text -> put("text", input.text)
                is ChatCompletionToolUse.Input.Number -> put("number", input.number)
                is ChatCompletionToolUse.Input.Boolean -> put("boolean", input.boolean)
                is ChatCompletionToolUse.Input.Array -> put("array", input.items.map(::buildContentToolUseInput))
                is ChatCompletionToolUse.Input.Object -> input.items.forEach {
                    put(it.key, buildContentToolUseInput(it.value))
                }
            }
        }
    }

    private fun buildContentToolUseInput(input: ChatCompletionToolUse.Input): Any {
        return when (input) {
            is ChatCompletionToolUse.Input.Text -> input.text
            is ChatCompletionToolUse.Input.Number -> input.number
            is ChatCompletionToolUse.Input.Boolean -> input.boolean
            is ChatCompletionToolUse.Input.Array -> input.items.map(::buildContentToolUseInput)
            is ChatCompletionToolUse.Input.Object -> buildContentToolUseInputToMap(input)
        }
    }

    private fun buildContentToolResultOutput(output: List<ChatCompletionToolUseResult.Output>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val resultOutput = mutableListOf<Any>()
        for (item in output) {
            resultOutput += when (item) {
                is ChatCompletionToolUseResult.Output.Text -> item.text
                is ChatCompletionToolUseResult.Output.Image -> mapOf(
                    "data" to item.data,
                    "mediaType" to item.mediaType.toMimeType()
                )
            }
        }
        result["output"] = resultOutput
        return result
    }

    private fun buildConfig(request: ChatCompletionRequest): GenerateContentConfig {
        val mode = when(request.tools.choice) {
            ChatCompletionRequest.Tools.ToolChoice.AUTO -> "AUTO"
            ChatCompletionRequest.Tools.ToolChoice.NONE -> "NONE"
            ChatCompletionRequest.Tools.ToolChoice.REQUIRED -> "ANY"
        }
        return GenerateContentConfig.builder()
            .temperature(request.temperature.toFloat())
            .systemInstruction(Content.fromParts(Part.fromText(request.instructions)))
            .maxOutputTokens(request.maxOutputTokens.toInt())
            .apply {
                if (request.tools.functions.isNotEmpty()) {
                    tools(buildTools(request.tools))
                    toolConfig(
                        ToolConfig.builder().functionCallingConfig(
                            FunctionCallingConfig.builder().mode(mode).build()
                        ).build()
                    )
                }
            }
            .build()
    }

    private fun buildTools(tool: ChatCompletionRequest.Tools): List<Tool> {
        return listOf(
            Tool.builder().functionDeclarations(
                // Sort by name for deterministic ordering — Google's implicit caching hashes
                // the request prefix, so any reshuffle invalidates the cached prefix.
                tool.functions.sortedBy { it.name }.map { function ->
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

    private fun parameterToSchema(value: ParameterValue): Schema = when (value) {
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
            schema.build()
        }
        is ParameterValue.ArrayValue -> {
            val schema = Schema.builder()
                .type("array")
                .items(parameterToSchema(value.items))
            if (value.description != null) {
                schema.description(value.description)
            }
            schema.build()
        }
        is ParameterValue.StringValue -> {
            val schema = Schema.builder().type("string")
            if (value.description != null) {
                schema.description(value.description)
            }
            if (value.enum?.isNotEmpty() == true) {
                schema.enum_(value.enum)
            }
            schema.build()
        }
        is ParameterValue.NumberValue -> {
            val schema = Schema.builder().type("number")
            if (value.description != null) {
                schema.description(value.description)
            }
            schema.build()
        }
        is ParameterValue.BooleanValue -> {
            val schema = Schema.builder().type("boolean")
            if (value.description != null) {
                schema.description(value.description)
            }
            schema.build()
        }
    }

    private fun parseResponse(response: GenerateContentResponse) = chatCompletionResponse {
        val finishReason = response.candidates().getOrNull()?.first()?.finishReason()?.getOrNull()?.knownEnum()
        id = response.responseId().orElse("")
        stopReason = when (finishReason) {
            FinishReason.Known.STOP -> ChatCompletionResponse.StopReason.END_TURN
            FinishReason.Known.MAX_TOKENS -> ChatCompletionResponse.StopReason.MAX_TOKENS
            else -> ChatCompletionResponse.StopReason.UNKNOWN
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
                val promptTokensKnown = it.promptTokenCount().isPresent
                val promptTokens = it.promptTokenCount().orElse(-1).toLong()
                val cachedTokens = it.cachedContentTokenCount().orElse(0).toLong()
                // Google semantics: promptTokenCount includes cached content when cached_content is set.
                // Normalize to the "fresh" portion so our Usage model is consistent across providers.
                if (promptTokensKnown) {
                    inputTokens = (promptTokens - cachedTokens).coerceAtLeast(0)
                    cacheReadInputTokens = cachedTokens
                } else {
                    // Usage metadata missing — propagate "unknown" to all input-side metrics so that
                    // downstream aggregation doesn't misinterpret a default 0 as "no cache hit".
                    inputTokens = -1
                    cacheReadInputTokens = -1
                }
                if (it.thoughtsTokenCount().isEmpty && it.candidatesTokenCount().isEmpty) {
                    outputTokens = -1
                } else {
                    outputTokens = (it.thoughtsTokenCount().orElse(0) + it.candidatesTokenCount().orElse(0)).toLong()
                }
                // Google implicit caching doesn't expose a separate cache-write metric.
                cacheCreationInputTokens = 0
            }
        }
    }

    private fun GenericJsonInputBuilder.parseToolUseInput(input: Any?) {
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

    private fun ChatCompletionToolUseInputObjectBuilder.parseToolUseInput(key: Any?, value: Any?) {
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

    private fun ChatCompletionImageType.toMimeType() = when (this) {
        ChatCompletionImageType.JPEG -> "image/jpeg"
        ChatCompletionImageType.PNG -> "image/png"
        ChatCompletionImageType.GIF -> "image/gif"
        ChatCompletionImageType.WEBP -> "image/webp"
    }
}