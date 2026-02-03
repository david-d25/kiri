package space.davids_digital.kiri.integration.anthropic

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.*
import com.anthropic.models.*
import com.anthropic.models.messages.Base64ImageSource
import com.anthropic.models.messages.ContentBlockParam
import com.anthropic.models.messages.ImageBlockParam
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.MessageParam
import com.anthropic.models.messages.RedactedThinkingBlockParam
import com.anthropic.models.messages.ServerToolUseBlockParam
import com.anthropic.models.messages.StopReason.Companion.END_TURN
import com.anthropic.models.messages.StopReason.Companion.MAX_TOKENS
import com.anthropic.models.messages.StopReason.Companion.STOP_SEQUENCE
import com.anthropic.models.messages.StopReason.Companion.TOOL_USE
import com.anthropic.models.messages.TextBlockParam
import com.anthropic.models.messages.ThinkingBlockParam
import com.anthropic.models.messages.Tool
import com.anthropic.models.messages.ToolChoice
import com.anthropic.models.messages.ToolChoiceAny
import com.anthropic.models.messages.ToolChoiceAuto
import com.anthropic.models.messages.ToolChoiceNone
import com.anthropic.models.messages.ToolResultBlockParam
import com.anthropic.models.messages.ToolUnion
import com.anthropic.models.messages.ToolUseBlockParam
import com.anthropic.models.messages.WebSearchResultBlock
import com.anthropic.models.messages.WebSearchTool20250305
import com.anthropic.models.messages.WebSearchToolResultBlockContent
import com.anthropic.models.messages.WebSearchToolResultBlockParam
import com.anthropic.models.models.ModelInfo
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
import space.davids_digital.kiri.integration.ChatCompletionUtils.optimize
import space.davids_digital.kiri.integration.ChatCompletionUtils.parameterToJson
import space.davids_digital.kiri.integration.ChatCompletionUtils.toolUseInputToJson
import space.davids_digital.kiri.llm.*
import space.davids_digital.kiri.llm.ChatCompletionRequest.Message
import space.davids_digital.kiri.llm.ChatCompletionRequest.Tools.Function.ParameterValue
import space.davids_digital.kiri.llm.dsl.GenericJsonInputBuilder
import space.davids_digital.kiri.llm.dsl.ChatCompletionToolUseInputObjectBuilder
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

    private fun onApiKeyChange(newValue: String?) {
        log.info("Got new Anthropic API key")
        val apiKey = newValue?.takeIf { it.isNotBlank() }
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
        val params = buildParams(cleanup(optimize(request)))
        val response = client.messages().create(params)
        return parseResponse(response)
    }

    @Cacheable(value = ["AnthropicChatCompletionService#getModels"])
    override suspend fun getModels(): List<ChatCompletionModel> {
        val client = clientRef.get() ?: return emptyList()
        val models = client.models().list().data()
        return models.filter(::isSupportedModel).map {
            ChatCompletionModel(
                handle = "$serviceHandle/${it.id()}",
                reasoningType = getReasoningType(it.id()),
                features = ChatCompletionModel.Features(
                    reasoningMaxTokens = getReasoningMaxTokensSupported(it.id()),
                    webSearch = getWebSearchSupported(it.id())
                )
            )
        }
    }

    private fun cleanup(request: ChatCompletionRequest): ChatCompletionRequest {
        val cleanedMessages = request.messages.dropWhile {
            it.content.any {
                contentItem -> contentItem is ChatCompletionWebSearch
            }
        }
        return request.copy(messages = cleanedMessages)
    }

    private fun getReasoningType(modelId: String): ChatCompletionModel.ReasoningType {
        // https://platform.claude.com/docs/en/build-with-claude/extended-thinking#supported-models
        val supported = listOf(
            "claude-sonnet-4-5-20250929",
            "claude-sonnet-4-20250514",
            "claude-3-7-sonnet-20250219",
            "claude-haiku-4-5-20251001",
            "claude-opus-4-5-20251101",
            "claude-opus-4-1-20250805",
            "claude-opus-4-20250514"
        )
        if (supported.contains(modelId)) {
            return ChatCompletionModel.ReasoningType.OPTIONAL
        }
        return ChatCompletionModel.ReasoningType.NONE
    }

    private fun getReasoningMaxTokensSupported(modelId: String): Boolean {
        return getReasoningType(modelId) != ChatCompletionModel.ReasoningType.NONE
    }

    private fun getWebSearchSupported(modelId: String): Boolean {
        // https://platform.claude.com/docs/en/agents-and-tools/tool-use/web-search-tool#supported-models
        val supported = listOf(
            "claude-sonnet-4-5-20250929",
            "claude-sonnet-4-20250514",
            "claude-3-7-sonnet-20250219",
            "claude-haiku-4-5-20251001",
            "claude-3-5-haiku-latest",
            "claude-opus-4-5-20251101",
            "claude-opus-4-1-20250805",
            "claude-opus-4-20250514"
        )
        return supported.contains(modelId)
    }

    @Cacheable(
        value = ["AnthropicChatCompletionService#getStatus"],
        unless = "#result != T(space.davids_digital.nod3r.model.ExternalServiceGatewayStatus).READY",
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
        require(provider == serviceHandle) { "Unsupported model provider: $provider" }
        require(isSupportedModelId(model)) { "Unsupported model: $model" }
        require(request.messages.isNotEmpty()) { "At least one message is required" }
        require(!request.reasoning.enabled || request.reasoning.maxTokens > 0) {
            "Reasoning max tokens must be greater than zero when reasoning is enabled"
        }
        model(model)
        system(request.instructions)
        maxTokens(request.maxOutputTokens)
        messages(buildMessages(request.messages))
        tools(buildTools(request.tools))
        if (request.reasoning.enabled) {
            enabledThinking(request.reasoning.maxTokens)
        }
        if (request.reasoning.enabled) {
            temperature(1.0)
        } else {
            temperature(request.temperature)
        }
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

    private fun parseResponse(response: com.anthropic.models.messages.Message) = chatCompletionResponse {
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
                } else if (contentBlock.isThinking()) {
                    val thinking = contentBlock.asThinking()
                    reasoning {
                        content = thinking.thinking()
                        signature = thinking.signature()
                    }
                } else if (contentBlock.isRedactedThinking()) {
                    val redactedThinking = contentBlock.asRedactedThinking()
                    redactedReasoning(redactedThinking.data())
                } else if (contentBlock.isServerToolUse()) {
                    val serverToolUse = contentBlock.asServerToolUse()
                    when (serverToolUse._name().toString()) {
                        "web_search" -> {
                            webSearchSearch(
                                serverToolUse.id(),
                                serverToolUse._input().asObject().getOrNull()?.get("query")?.toString() ?: "",
                                emptyList()
                            )
                        }
                        else -> {
                            log.warn("Unsupported server tool use name: {}", serverToolUse._name())
                        }
                    }
                } else if (contentBlock.isWebSearchToolResult()) {
                    val webSearchToolResult = contentBlock.asWebSearchToolResult()
                    val content = webSearchToolResult.content()
                    if (content.isError()) {
                        val error = content.asError()
                        log.warn("Web search tool result error code {}", error.errorCode())
                    } else {
                        val blocks = content.asResultBlocks()
                        webSearchOpenPage {
                            id = webSearchToolResult.toolUseId()
                            url = "" // URLs are for each block individually
                            for (block in blocks) {
                                content(
                                    block.url(),
                                    block.title(),
                                    block.encryptedContent(),
                                    block.pageAge().getOrNull()
                                )
                            }
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

    private fun ChatCompletionToolUseInputObjectBuilder.parseToolUseInput(key: String, input: JsonValue) {
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
                    is Message.ContentItem.Reasoning -> ContentBlockParam.ofThinking(
                        ThinkingBlockParam.builder()
                            .thinking(
                                contentItem.content
                                    ?: error("Reasoning content cannot be null for Anthropic integration")
                            )
                            .signature(contentItem.signature)
                            .build()
                    )
                    is Message.ContentItem.RedactedReasoning -> ContentBlockParam.ofRedactedThinking(
                        RedactedThinkingBlockParam.builder()
                            .data(contentItem.data)
                            .build()
                    )
                    is ChatCompletionWebSearch.Search -> ContentBlockParam.ofServerToolUse(
                        ServerToolUseBlockParam.builder()
                            .id(contentItem.id ?: error("WebSearch Search content item must have an ID"))
                            .input(
                                ServerToolUseBlockParam.Input.builder()
                                    .putAdditionalProperty("query", JsonString.of(contentItem.query))
                                    .build()
                            )
                            .build()
                    )
                    is ChatCompletionWebSearch.OpenPage -> ContentBlockParam.ofWebSearchToolResult(
                        WebSearchToolResultBlockParam.builder()
                            .toolUseId(contentItem.id ?: error("WebSearch Search content item must have an ID"))
                            .content(
                                WebSearchToolResultBlockContent.ofResultBlocks(
                                    contentItem.content.map { page ->
                                        WebSearchResultBlock.builder()
                                            .url(page.url)
                                            .title(page.title)
                                            .encryptedContent(page.encryptedContent)
                                            .pageAge(page.pageAge)
                                            .build()
                                    }
                                )
                            )
                            .build()
                    )
                    is ChatCompletionWebSearch.FindInPage ->
                        error("Anthropic integration does not support WebSearch FindInPage content item")
                }
            }
        ))
        builder.build()
    }

    private fun buildTools(tools: ChatCompletionRequest.Tools) = buildList {
        tools.functions.forEach { function ->
            this += ToolUnion.ofTool(
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
        if (tools.external.webSearch.enabled) {
            this += ToolUnion.ofWebSearchTool20250305(
                WebSearchTool20250305.builder().build()
            )
        }
    }

    private fun buildInputSchema(parameters: ParameterValue.ObjectValue?): Tool.InputSchema {
        if (parameters == null) {
            return Tool.InputSchema.builder().type(JsonValue.from("object")).build()
        }
        return Tool.InputSchema.builder()
            .type(JsonValue.from("object"))
            .required(parameters.required)
            .properties(JsonValue.from(parameters.properties.mapValues { parameterToJson(it.value) }))
            .build()
    }

    private fun toolUseResultOutputToContentBlockList(
        toolUseResult: List<ChatCompletionToolUseResult.Output>
    ): List<ToolResultBlockParam.Content.Block> {
        return toolUseResult.map { item ->
            when (item) {
                is ChatCompletionToolUseResult.Output.Text -> ToolResultBlockParam.Content.Block.ofText(
                    TextBlockParam.builder().text(item.text).build()
                )

                is ChatCompletionToolUseResult.Output.Image -> ToolResultBlockParam.Content.Block.ofImage(
                    ImageBlockParam.builder()
                        .source(
                            Base64ImageSource.builder()
                                .data(Base64.getEncoder().encodeToString(item.data))
                                .mediaType(mapImageMediaType(item.mediaType))
                                .build()
                        )
                        .build()
                )
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

    private fun mapImageMediaType(mediaType: ChatCompletionImageType) = when (mediaType) {
        ChatCompletionImageType.JPEG -> Base64ImageSource.MediaType.IMAGE_JPEG
        ChatCompletionImageType.PNG -> Base64ImageSource.MediaType.IMAGE_PNG
        ChatCompletionImageType.GIF -> Base64ImageSource.MediaType.IMAGE_GIF
        ChatCompletionImageType.WEBP -> Base64ImageSource.MediaType.IMAGE_WEBP
    }
}