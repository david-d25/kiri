package space.davids_digital.kiri.integration.anthropic

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.*
import com.anthropic.models.*
import com.anthropic.models.messages.Base64ImageSource
import com.anthropic.models.messages.CacheControlEphemeral
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
import com.anthropic.models.messages.ThinkingConfigAdaptive
import com.anthropic.models.messages.ThinkingConfigEnabled
import com.anthropic.models.messages.ThinkingConfigParam
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
import space.davids_digital.kiri.orm.service.LlmUsageStatOrmService
import space.davids_digital.kiri.orm.service.SettingOrmService
import space.davids_digital.kiri.service.ChatCompletionService
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import kotlin.jvm.optionals.getOrNull
import kotlin.math.sqrt

@Service
class AnthropicChatCompletionService(
    private val settings: SettingOrmService,
    private val usageStats: LlmUsageStatOrmService,
) : ChatCompletionService {
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
        val params = buildParams(mergeConsecutiveMessages(cleanup(optimize(request))))
        val startedAt = System.nanoTime()
        val response = client.messages().create(params)
        val parsed = parseResponse(response)
        val durationMs = (System.nanoTime() - startedAt) / 1_000_000
        val modelId = request.modelHandle.substringAfter('/')
        usageStats.record(
            provider = serviceHandle,
            model = modelId,
            inputTokens = parsed.usage.inputTokens,
            outputTokens = parsed.usage.outputTokens,
            cacheReadInputTokens = parsed.usage.cacheReadInputTokens,
            cacheCreationInputTokens = parsed.usage.cacheCreationInputTokens,
            durationMs = durationMs,
        )
        return parsed
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

    /**
     * Merges consecutive messages with the same role into a single message.
     * Required because the Anthropic API does not allow consecutive messages with the same role,
     * and in particular requires thinking blocks to be in the same assistant message as tool_use blocks.
     */
    private fun mergeConsecutiveMessages(request: ChatCompletionRequest): ChatCompletionRequest {
        if (request.messages.size <= 1) return request
        val merged = mutableListOf<Message>()
        for (message in request.messages) {
            val last = merged.lastOrNull()
            if (last != null && last.role == message.role) {
                merged[merged.lastIndex] = last.copy(content = last.content + message.content)
            } else {
                merged.add(message)
            }
        }
        return request.copy(messages = merged)
    }

    private fun getReasoningType(modelId: String): ChatCompletionModel.ReasoningType {
        // https://platform.claude.com/docs/en/build-with-claude/extended-thinking#supported-models
        val supported = listOf(
            "claude-sonnet-4-5-20250929",
            "claude-sonnet-4-20250514",
            "claude-3-7-sonnet-20250219",
            "claude-haiku-4-5-20251001",
            "claude-opus-4-20250514",
            "claude-opus-4-1-20250805",
            "claude-opus-4-5-20251101",
            "claude-opus-4-6",
            "claude-opus-4-7",
            "claude-sonnet-4-6",
        )
        if (supported.contains(modelId)) {
            return ChatCompletionModel.ReasoningType.OPTIONAL
        }
        return ChatCompletionModel.ReasoningType.NONE
    }

    private fun getReasoningMaxTokensSupported(modelId: String): Boolean {
        if (getReasoningType(modelId) == ChatCompletionModel.ReasoningType.NONE) return false
        // Adaptive thinking models decide the budget themselves — `budget_tokens` is either
        // ignored (Opus/Sonnet 4.6) or rejected (Opus 4.7).
        if (useAdaptiveThinking(modelId)) return false
        return true
    }

    private fun getWebSearchSupported(modelId: String): Boolean {
        // https://platform.claude.com/docs/en/agents-and-tools/tool-use/web-search-tool#supported-models
        val supported = listOf(
            "claude-sonnet-4-5-20250929",
            "claude-sonnet-4-20250514",
            "claude-3-7-sonnet-20250219",
            "claude-haiku-4-5-20251001",
            "claude-3-5-haiku-latest",
            "claude-opus-4-20250514",
            "claude-opus-4-1-20250805",
            "claude-opus-4-5-20251101",
            "claude-opus-4-6",
            "claude-opus-4-7",
            "claude-sonnet-4-6",
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

    /**
     * True for models that should use `thinking.type=adaptive` instead of the legacy
     * `thinking.type=enabled` with `budget_tokens`.
     *
     * - On Claude Opus 4.7, adaptive is the ONLY supported mode (enabled → 400).
     * - On Claude Opus 4.6 and Sonnet 4.6, enabled is deprecated but still works;
     *   we migrate proactively per Anthropic's recommendation.
     * - Older models (Sonnet 4.5, Opus 4.5, Sonnet 3.5/3.7, …) do not support adaptive
     *   and must continue using `enabled` with `budget_tokens`.
     *
     * See: https://platform.claude.com/docs/en/build-with-claude/adaptive-thinking
     */
    private fun useAdaptiveThinking(modelId: String): Boolean {
        val adaptiveCapable = listOf(
            "claude-opus-4-6",
            "claude-opus-4-7",
            "claude-sonnet-4-6",
        )
        return adaptiveCapable.any { modelId.startsWith(it) }
    }

    /**
     * True for models that reject any `temperature` other than 1.0 (returns 400).
     * Currently Opus 4.7+; older models including Opus/Sonnet 4.6 still accept temperature.
     */
    private fun rejectsNonDefaultTemperature(modelId: String): Boolean {
        val rejectsTemperature = listOf(
            "claude-opus-4-7",
        )
        return rejectsTemperature.any { modelId.startsWith(it) }
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
        systemOfTextBlockParams(listOf(
            TextBlockParam.builder()
                .text(request.instructions)
                .cacheControl(CacheControlEphemeral.builder().build())
                .build()
        ))
        maxTokens(request.maxOutputTokens)
        messages(buildMessages(request.messages))
        tools(buildTools(request.tools))
        val adaptiveThinking = useAdaptiveThinking(model)
        if (request.reasoning.enabled) {
            thinking(
                if (adaptiveThinking) {
                    ThinkingConfigParam.ofAdaptive(
                        ThinkingConfigAdaptive.builder()
                            .display(ThinkingConfigAdaptive.Display.SUMMARIZED)
                            .build()
                    )
                } else {
                    ThinkingConfigParam.ofEnabled(
                        ThinkingConfigEnabled.builder()
                            .budgetTokens(request.reasoning.maxTokens)
                            .display(ThinkingConfigEnabled.Display.SUMMARIZED)
                            .build()
                    )
                }
            )
        }
        if (!rejectsNonDefaultTemperature(model)) {
            @Suppress("DEPRECATION")
            temperature(if (request.reasoning.enabled) 1.0 else request.temperature)
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
            cacheReadInputTokens = response.usage().cacheReadInputTokens().orElse(0L)
            cacheCreationInputTokens = response.usage().cacheCreationInputTokens().orElse(0L)
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

    // Minimum gap, in RAW content blocks, between the tail breakpoint and the intermediate
    // one. Anthropic's prefix-lookback window is ~20 total content blocks per breakpoint
    // (thinking/redacted/server-tool/web-search blocks all count, not just cacheable ones),
    // so 10 leaves headroom for ticks that append several raw blocks (reasoning + parallel
    // tool calls + tool results).
    private val intermediateCacheBlockGap = 10

    private fun buildMessages(messages: List<Message>): List<MessageParam> {
        val breakpoints = pickCacheBreakpoints(messages)
        return messages.mapIndexed { mi, message ->
            val builder = MessageParam.builder()
            when (message.role) {
                Message.Role.USER -> builder.role(MessageParam.Role.USER)
                Message.Role.ASSISTANT -> builder.role(MessageParam.Role.ASSISTANT)
            }
            builder.content(MessageParam.Content.ofBlockParams(
                message.content.mapIndexed { bi, contentItem ->
                    val cache = (mi to bi) in breakpoints
                    when (contentItem) {
                        is Message.ContentItem.Text -> ContentBlockParam.ofText(
                            TextBlockParam.builder().text(contentItem.text).apply {
                                if (cache) cacheControl(CacheControlEphemeral.builder().build())
                            }.build()
                        )
                        is Message.ContentItem.Image -> ContentBlockParam.ofImage(
                            buildImageBlock(contentItem, cache)
                        )
                        is Message.ContentItem.ToolUse -> ContentBlockParam.ofToolUse(
                            ToolUseBlockParam.builder()
                                .id(contentItem.toolUse.id)
                                .name(contentItem.toolUse.name)
                                .input(JsonValue.fromJsonNode(toolUseInputToJson(contentItem.toolUse.input)))
                                .apply { if (cache) cacheControl(CacheControlEphemeral.builder().build()) }
                                .build()
                        )
                        is Message.ContentItem.ToolResult -> ContentBlockParam.ofToolResult(
                            ToolResultBlockParam.builder()
                                .toolUseId(contentItem.toolResult.toolUseId)
                                .contentOfBlocks(toolUseResultOutputToContentBlockList(contentItem.toolResult.output))
                                .apply { if (cache) cacheControl(CacheControlEphemeral.builder().build()) }
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
                                .name(ServerToolUseBlockParam.Name.WEB_SEARCH)
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
    }

    private fun isMessageCacheable(item: Message.ContentItem): Boolean = when (item) {
        is Message.ContentItem.Text -> true
        is Message.ContentItem.Image -> true
        is Message.ContentItem.ToolUse -> true
        is Message.ContentItem.ToolResult -> true
        is Message.ContentItem.Reasoning -> false
        is Message.ContentItem.RedactedReasoning -> false
        is ChatCompletionWebSearch.Search -> false
        is ChatCompletionWebSearch.OpenPage -> false
        is ChatCompletionWebSearch.FindInPage -> false
    }

    /**
     * Picks up to two cache breakpoints in the message history:
     *  - tail: last cacheable block (caches the current state for the next tick to read);
     *  - intermediate: last cacheable block sitting ≥[intermediateCacheBlockGap] RAW blocks
     *    before the tail. Survives both the ~20-block prefix lookback and tail rewrites
     *    (retries, last-frame edits).
     *
     * Skipped when the intermediate would land on the very first cacheable block of the
     * history — that prefix is already covered by the system prompt's breakpoint, so the
     * slot would yield no incremental cache benefit.
     */
    private fun pickCacheBreakpoints(messages: List<Message>): Set<Pair<Int, Int>> {
        var tailMsg = -1
        var tailBlock = -1
        var tailRaw = -1
        var firstCacheableRaw = -1
        var raw = 0
        messages.forEachIndexed { mi, msg ->
            msg.content.forEachIndexed { bi, item ->
                if (isMessageCacheable(item)) {
                    if (firstCacheableRaw < 0) firstCacheableRaw = raw
                    tailMsg = mi; tailBlock = bi; tailRaw = raw
                }
                raw++
            }
        }
        if (tailMsg < 0) return emptySet()
        val out = HashSet<Pair<Int, Int>>(2).apply { add(tailMsg to tailBlock) }
        val cutoff = tailRaw - intermediateCacheBlockGap
        if (cutoff < 0) return out
        var interMsg = -1
        var interBlock = -1
        var interRaw = -1
        raw = 0
        outer@ for (mi in messages.indices) {
            val content = messages[mi].content
            for (bi in content.indices) {
                if (raw > cutoff) break@outer
                if (isMessageCacheable(content[bi])) {
                    interMsg = mi; interBlock = bi; interRaw = raw
                }
                raw++
            }
        }
        if (interMsg >= 0 && interRaw > firstCacheableRaw) {
            out.add(interMsg to interBlock)
        }
        return out
    }

    private fun buildTools(tools: ChatCompletionRequest.Tools) = buildList {
        // Sort by name for deterministic ordering — necessary for prompt caching stability,
        // since any tool-list reshuffle invalidates the cached [tools+system] prefix.
        tools.functions.sortedBy { it.name }.forEach { function ->
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

                is ChatCompletionToolUseResult.Output.Image -> {
                    val (data, mediaType) = clampImageForAnthropic(item.data, item.mediaType)
                    ToolResultBlockParam.Content.Block.ofImage(
                        ImageBlockParam.builder()
                            .source(
                                Base64ImageSource.builder()
                                    .data(Base64.getEncoder().encodeToString(data))
                                    .mediaType(mapImageMediaType(mediaType))
                                    .build()
                            )
                            .build()
                    )
                }
            }
        }
    }

    private fun buildImageBlock(
        imageContentItem: Message.ContentItem.Image,
        cache: Boolean = false,
    ): ImageBlockParam {
        val (data, mediaType) = clampImageForAnthropic(imageContentItem.data, imageContentItem.mediaType)
        return ImageBlockParam.builder().apply {
            source(
                Base64ImageSource.builder()
                    .data(Base64.getEncoder().encodeToString(data))
                    .mediaType(mapImageMediaType(mediaType))
                    .build()
            )
            if (cache) cacheControl(CacheControlEphemeral.builder().build())
        }.build()
    }

    // Anthropic rejects images whose base64 payload exceeds 5 MiB. Base64 expands by ~4/3,
    // so raw bytes must stay under ~3.93 MB; we use a slightly tighter cap for safety.
    private val maxAnthropicImageRawBytes = 3_700_000

    private fun clampImageForAnthropic(
        data: ByteArray,
        mediaType: ChatCompletionImageType,
    ): Pair<ByteArray, ChatCompletionImageType> {
        if (data.size <= maxAnthropicImageRawBytes) return data to mediaType
        val original = try {
            ImageIO.read(ByteArrayInputStream(data))
        } catch (e: Exception) {
            log.warn("Failed to decode oversized image (${data.size} bytes), sending as-is", e)
            return data to mediaType
        }
        if (original == null) {
            log.warn("ImageIO returned null for oversized image (${data.size} bytes), sending as-is")
            return data to mediaType
        }
        var scale = sqrt(maxAnthropicImageRawBytes.toDouble() / data.size).coerceAtMost(1.0)
        repeat(6) {
            val newWidth = (original.width * scale).toInt().coerceAtLeast(64)
            val newHeight = (original.height * scale).toInt().coerceAtLeast(64)
            val resized = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
            val g = resized.createGraphics()
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                // TYPE_INT_RGB has no alpha; fill white so transparent pixels don't render as black.
                g.color = java.awt.Color.WHITE
                g.fillRect(0, 0, newWidth, newHeight)
                g.drawImage(original, 0, 0, newWidth, newHeight, null)
            } finally {
                g.dispose()
            }
            val baos = ByteArrayOutputStream()
            ImageIO.write(resized, "jpeg", baos)
            val out = baos.toByteArray()
            if (out.size <= maxAnthropicImageRawBytes) {
                log.info(
                    "Downscaled image from {} bytes ({}x{}) to {} bytes ({}x{}) for Anthropic 5MB limit",
                    data.size, original.width, original.height, out.size, newWidth, newHeight
                )
                return out to ChatCompletionImageType.JPEG
            }
            scale *= 0.8
        }
        log.warn(
            "Failed to fit image under {} bytes after downscaling attempts; sending original ({} bytes)",
            maxAnthropicImageRawBytes, data.size
        )
        return data to mediaType
    }

    private fun mapImageMediaType(mediaType: ChatCompletionImageType) = when (mediaType) {
        ChatCompletionImageType.JPEG -> Base64ImageSource.MediaType.IMAGE_JPEG
        ChatCompletionImageType.PNG -> Base64ImageSource.MediaType.IMAGE_PNG
        ChatCompletionImageType.GIF -> Base64ImageSource.MediaType.IMAGE_GIF
        ChatCompletionImageType.WEBP -> Base64ImageSource.MediaType.IMAGE_WEBP
    }
}