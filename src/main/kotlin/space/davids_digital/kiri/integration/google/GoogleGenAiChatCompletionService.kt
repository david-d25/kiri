package space.davids_digital.kiri.integration.google

import com.google.genai.Client
import com.google.genai.types.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import space.davids_digital.kiri.aop.EvictCacheOnException
import space.davids_digital.kiri.llm.*
import space.davids_digital.kiri.llm.ChatCompletionRequest.Message
import space.davids_digital.kiri.llm.ChatCompletionRequest.Tools.Function.ParameterValue
import space.davids_digital.kiri.llm.dsl.GenericJsonInputBuilder
import space.davids_digital.kiri.llm.dsl.LlmToolUseInputObjectBuilder
import space.davids_digital.kiri.llm.dsl.chatCompletionResponse
import space.davids_digital.kiri.model.ChatCompletionModel
import space.davids_digital.kiri.model.ExternalServiceGatewayStatus
import space.davids_digital.kiri.model.Setting
import space.davids_digital.kiri.orm.service.SettingOrmService
import space.davids_digital.kiri.service.ChatCompletionService
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.optionals.getOrNull

@Service
class GoogleGenAiChatCompletionService(private val settings: SettingOrmService) : ChatCompletionService {
    object SettingKeys {
        const val API_KEY = "integration.google.genAi.apiKey"
    }

    override val serviceHandle = "google-genai"

    class ServiceDisabledException : RuntimeException()

    private val log = LoggerFactory.getLogger(this::class.java)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clientRef = AtomicReference<Client?>()

    @PostConstruct
    private fun init() {
        scope.launch {
            settings.listen(SettingKeys.API_KEY).collectLatest {
                try {
                    onApiKeyChange(it)
                } catch (e: Exception) {
                    log.error("Failed to handle Google GenAI API Key change", e)
                }
            }
        }
    }

    private fun onApiKeyChange(setting: Setting) {
        log.info("Got new Google GenAI API key")
        val apiKey = setting.value?.takeIf { it.isNotBlank() }
        if (apiKey == null) {
            log.warn("Google GenAI API key is empty, client will be disabled")
            clientRef.set(null)
            return
        }
        clientRef.set(buildClient(apiKey))
        log.info("Google GenAI client created")
    }

    private fun buildClient(apiKey: String): Client {
        return Client.builder().apiKey(apiKey).build()
    }

    private fun requireClient() = clientRef.get() ?: throw ServiceDisabledException()

    @EvictCacheOnException(cacheNames = ["GoogleGenAiChatCompletionService#getStatus"])
    override suspend fun request(request: ChatCompletionRequest): ChatCompletionResponse {
        val client = requireClient()
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
        val response = client.models.generateContent(sdkModel, content, config)
        return parseResponse(response)
    }

    @Cacheable(
        value = ["GoogleGenAiChatCompletionService#getStatus"],
        unless = "#result != T(space.davids_digital.kiri.model.ExternalServiceGatewayStatus).READY",
        cacheManager = "oneHour"
    )
    override suspend fun getStatus(): ExternalServiceGatewayStatus {
        val client = clientRef.get()
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
        val client = clientRef.get() ?: return emptyList()
        val model = client.models.list(ListModelsConfig.builder().pageSize(100).build())
        return model.filter { it.name().isPresent && isSupportedModelId(it.name().get()) }.map {
            ChatCompletionModel(
                handle = serviceHandle + "/" + it.name().get().removePrefix("models/")
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

    private fun buildContentToolResultOutput(output: ChatCompletionToolUseResult.Output): Map<String, Any> {
        return when (output) {
            is ChatCompletionToolUseResult.Output.Text -> mapOf("output" to output.text)
            is ChatCompletionToolUseResult.Output.Image -> mapOf("output" to output.data)
        }
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
                            FunctionCallingConfig.builder().mode(mode).build() // TODO add 'allowParallelUse' when available
                        ).build()
                    )
                }
            }
            .build()
    }

    private fun buildTools(tool: ChatCompletionRequest.Tools): List<Tool> {
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
                inputTokens = it.promptTokenCount().orElse(-1).toLong()
                if (it.thoughtsTokenCount().isEmpty && it.candidatesTokenCount().isEmpty) {
                    outputTokens = -1
                } else {
                    outputTokens = (it.thoughtsTokenCount().orElse(0) + it.candidatesTokenCount().orElse(0)).toLong()
                }
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

    private fun ChatCompletionImageType.toMimeType() = when (this) {
        ChatCompletionImageType.JPEG -> "image/jpeg"
        ChatCompletionImageType.PNG -> "image/png"
        ChatCompletionImageType.GIF -> "image/gif"
        ChatCompletionImageType.WEBP -> "image/webp"
    }
}