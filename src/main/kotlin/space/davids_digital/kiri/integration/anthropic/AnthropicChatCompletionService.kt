package space.davids_digital.kiri.integration.anthropic

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.*
import com.anthropic.models.*
import com.anthropic.models.Message.StopReason.Companion.END_TURN
import com.anthropic.models.Message.StopReason.Companion.MAX_TOKENS
import com.anthropic.models.Message.StopReason.Companion.STOP_SEQUENCE
import com.anthropic.models.Message.StopReason.Companion.TOOL_USE
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
import space.davids_digital.kiri.integration.ChatCompletionUtils.parameterToJson
import space.davids_digital.kiri.integration.ChatCompletionUtils.toolUseInputToJson
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
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.optionals.getOrNull

@Service
class AnthropicChatCompletionService(private val settings: SettingOrmService) : ChatCompletionService {
    object SettingKeys {
        const val API_KEY = "integration.anthropic.apiKey"
    }

    override val serviceHandle = "anthropic"

    class ServiceDisabledException : RuntimeException()

    private val log = LoggerFactory.getLogger(this::class.java)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clientRef = AtomicReference<AnthropicClient?>()

    @PostConstruct
    private fun init() {
        scope.launch {
            settings.listen(SettingKeys.API_KEY).collectLatest {
                try {
                    onApiKeyChange(it)
                } catch (e: Exception) {
                    log.error("Failed to handle Anthropic API Key change", e)
                }
            }
        }
    }

    private fun onApiKeyChange(setting: Setting) {
        log.info("Got new Anthropic API key")
        val apiKey = setting.value?.takeIf { it.isNotBlank() }
        if (apiKey == null) {
            log.warn("Anthropic API key is empty, client will be disabled")
            clientRef.set(null)
            return
        }
        clientRef.set(buildClient(apiKey))
        log.info("Anthropic client created")
    }

    private fun buildClient(apiKey: String): AnthropicClient {
        return AnthropicOkHttpClient.builder().apiKey(apiKey).build()
    }

    private fun requireClient() = clientRef.get() ?: throw ServiceDisabledException()

    @EvictCacheOnException(cacheNames = ["AnthropicChatCompletionService#getStatus"])
    override suspend fun request(request: ChatCompletionRequest): ChatCompletionResponse {
        val client = requireClient()
        val params = buildParams(request)
        val response = client.messages().create(params)
        return parseResponse(response)
    }

    override suspend fun getModels(): List<ChatCompletionModel> {
        val client = clientRef.get() ?: return emptyList()
        val models = client.models().list().data()
        return models.filter(::isSupportedModel).map {
            ChatCompletionModel(
                handle = "$serviceHandle/${it.id()}",
            )
        }
    }

    @Cacheable(
        value = ["AnthropicChatCompletionService#getStatus"],
        unless = "#result != T(space.davids_digital.kiri.model.ExternalServiceGatewayStatus).READY",
        cacheManager = "oneHour"
    )
    override suspend fun getStatus(): ExternalServiceGatewayStatus {
        if (clientRef.get() == null) {
            return ExternalServiceGatewayStatus.DISABLED
        }
        return try {
            clientRef.get()!!.models().list()
            ExternalServiceGatewayStatus.READY
        } catch (e: Exception) {
            log.error("Anthropic service status check failed", e)
            ExternalServiceGatewayStatus.ERROR
        }
    }

    private fun isSupportedModel(model: ModelInfo): Boolean {
        return isSupportedModelId(model.id())
    }

    private fun isSupportedModelId(modelId: String): Boolean {
        return modelId.startsWith("claude-")
    }

    private fun buildParams(request: ChatCompletionRequest) = MessageCreateParams.builder().apply {
        val (provider, model) = request.modelHandle.split("/", limit = 2)
        if (provider != serviceHandle) {
            throw IllegalArgumentException("Unsupported model provider: $provider")
        }
        if (!isSupportedModelId(model)) {
            throw IllegalArgumentException("Unsupported model: $model")
        }
        model(model)
        system(request.instructions)
        maxTokens(request.maxOutputTokens)
        temperature(request.temperature)
        messages(buildMessages(request.messages))
        tools(buildTools(request.tools))
        toolChoice(
            when (request.tools.choice) {
                ChatCompletionRequest.Tools.ToolChoice.AUTO -> ToolChoice.ofAuto(
                    ToolChoiceAuto.builder()
                        .disableParallelToolUse(!request.tools.allowParallelUse)
                        .build()
                )
                ChatCompletionRequest.Tools.ToolChoice.REQUIRED -> ToolChoice.ofAny(
                    ToolChoiceAny.builder()
                        .disableParallelToolUse(!request.tools.allowParallelUse)
                        .build()
                )
                ChatCompletionRequest.Tools.ToolChoice.NONE -> ToolChoice.ofNone(ToolChoiceNone.builder().build())
            }
        )
    }.build()

    private fun parseResponse(response: com.anthropic.models.Message) = chatCompletionResponse {
        id = response.id()
        stopReason = when (response.stopReason().getOrNull()) {
            END_TURN -> ChatCompletionResponse.StopReason.END_TURN
            MAX_TOKENS -> ChatCompletionResponse.StopReason.MAX_TOKENS
            STOP_SEQUENCE -> ChatCompletionResponse.StopReason.STOP_SEQUENCE
            TOOL_USE -> ChatCompletionResponse.StopReason.TOOL_USE
            null -> null
            else -> ChatCompletionResponse.StopReason.UNKNOWN
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

    private fun GenericJsonInputBuilder.parseToolUseInput(input: JsonValue) {
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
            is JsonMissing, is JsonNull -> {}
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

    private fun buildTools(tools: ChatCompletionRequest.Tools) = tools.functions.map { function ->
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
        toolUseResult: ChatCompletionToolUseResult.Output
    ): List<ToolResultBlockParam.Content.Block> {
        return listOf(
            when(toolUseResult) {
                is ChatCompletionToolUseResult.Output.Text -> ToolResultBlockParam.Content.Block.ofTextBlockParam(
                    TextBlockParam.builder().text(toolUseResult.text).build()
                )
                is ChatCompletionToolUseResult.Output.Image -> ToolResultBlockParam.Content.Block.ofImageBlockParam(
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

    private fun buildImageBlock(imageContentItem: Message.ContentItem.Image) = ImageBlockParam.builder().apply {
        source(
            Base64ImageSource.builder()
                .data(Base64.getEncoder().encodeToString(imageContentItem.data))
                .mediaType(mapImageMediaType(imageContentItem.mediaType))
                .build()
        )
    }.build()

    private fun mapImageMediaType(mediaType: ChatCompletionImageType) = when (mediaType) {
        ChatCompletionImageType.JPEG -> Base64ImageSource.MediaType.IMAGE_JPEG
        ChatCompletionImageType.PNG -> Base64ImageSource.MediaType.IMAGE_PNG
        ChatCompletionImageType.GIF -> Base64ImageSource.MediaType.IMAGE_GIF
        ChatCompletionImageType.WEBP -> Base64ImageSource.MediaType.IMAGE_WEBP
    }
}