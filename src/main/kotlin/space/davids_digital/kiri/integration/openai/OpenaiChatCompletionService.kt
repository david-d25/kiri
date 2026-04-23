package space.davids_digital.kiri.integration.openai

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.openai.core.JsonObject
import com.openai.core.JsonValue
import com.openai.errors.OpenAIInvalidDataException
import com.openai.models.Reasoning
import com.openai.models.ReasoningEffort
import com.openai.models.responses.*
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import space.davids_digital.kiri.aop.EvictCacheOnException
import space.davids_digital.kiri.integration.ChatCompletionUtils.optimize
import space.davids_digital.kiri.integration.ChatCompletionUtils.parameterToJson
import space.davids_digital.kiri.integration.ChatCompletionUtils.toolUseInputToJson
import space.davids_digital.kiri.llm.*
import space.davids_digital.kiri.llm.ChatCompletionRequest.Message.ContentItem
import space.davids_digital.kiri.llm.ChatCompletionRequest.Tools.Function.ParameterValue
import space.davids_digital.kiri.llm.dsl.ChatCompletionToolUseInputObjectBuilder
import space.davids_digital.kiri.llm.dsl.GenericJsonInputBuilder
import space.davids_digital.kiri.llm.dsl.chatCompletionResponse
import space.davids_digital.kiri.model.ChatCompletionModel
import space.davids_digital.kiri.model.ExternalServiceGatewayStatus
import space.davids_digital.kiri.orm.service.LlmUsageStatOrmService
import space.davids_digital.kiri.service.ChatCompletionService
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class OpenaiChatCompletionService(
    private val clientHolder: OpenaiClientHolder,
    private val objectMapper: ObjectMapper,
    private val usageStats: LlmUsageStatOrmService,
) : ChatCompletionService {
    override val serviceHandle = "openai-chat-completion"

    private val log = LoggerFactory.getLogger(javaClass)

    @EvictCacheOnException(cacheNames = ["OpenAIChatCompletionService#getStatus"])
    override suspend fun request(request: ChatCompletionRequest): ChatCompletionResponse {
        val client = clientHolder.requireClient()
        val optimizedRequest = optimize(request)
        val params = buildParams(optimizedRequest)
        log.info("Requesting OpenAI chat completion with model={}", params.model().getOrNull()?.asString())
        val startedAt = System.nanoTime()
        val response = client.responses().create(params)
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
        return cleanUp(parsed)
    }

    @Cacheable(
        value = ["OpenAIChatCompletionService#getStatus"],
        unless = "#result != T(space.davids_digital.nod3r.model.ExternalServiceGatewayStatus).READY",
        cacheManager = "oneHour"
    )
    override suspend fun getStatus(): ExternalServiceGatewayStatus {
        val client = clientHolder.getClient() ?: return ExternalServiceGatewayStatus.DISABLED
        return try {
            client.models().list().data()
            ExternalServiceGatewayStatus.READY
        } catch (e: Exception) {
            log.error("Failed to get OpenAI service status", e)
            ExternalServiceGatewayStatus.ERROR
        }
    }

    override suspend fun getModels(): List<ChatCompletionModel> {
        val client = clientHolder.getClient() ?: return emptyList()
        val models = client.models().list().data()
        return models
            .map { it.id() }
            .mapNotNull { toModelOrNull(it) }
    }

    private fun toModelOrNull(modelId: String): ChatCompletionModel? {
        if (!isSupportedModelId(modelId)) {
            return null
        }
        val handle = "$serviceHandle/$modelId"
        return ChatCompletionModel(
            handle = handle,
            reasoningType = getReasoningType(modelId),
            features = ChatCompletionModel.Features(
                webSearch = isWebSearchSupported(modelId),
                reasoningEffort = isReasoningEffortSupported(modelId)
            )
        )
    }

    private fun isWebSearchSupported(modelId: String): Boolean {
        // gpt-5 does not support web search with 'minimal' reasoning effort
        val notSupportedList = listOf("gpt-4.1-nano", "gpt-5", "o1", "o3-mini")
        return !notSupportedList.contains(modelId)
    }

    private fun isReasoningSupported(modelId: String): Boolean {
        return modelId.removePrefix("ft:").matches(Regex("^(o1.*|o3.*|o4.*|gpt-5.*)$"))
    }

    private fun isReasoningRequired(modelId: String): Boolean {
        return modelId.removePrefix("ft:").matches(Regex("^(o1.*|o3.*|o4.*)$"))
    }

    private fun isSupportedModelId(modelId: String): Boolean {
        return modelId.removePrefix("ft:").matches(Regex("^(gpt-4o|gpt-4.1|gpt-5|gpt-5\\.1|gpt-5\\.2|o1|o3|o4)(-mini|-nano)?$"))
    }

    private fun isReasoningEffortSupported(modelId: String): Boolean {
        return modelId.removePrefix("ft:").matches(Regex("^(gpt-5|gpt-5.1|gpt-5.2|o1|o3|o4)(-mini|-nano)?$"))
    }

    private fun isTemperatureSupported(modelId: String): Boolean {
        return modelId.removePrefix("ft:").matches(Regex("^(gpt-4o|gpt-4.1)(-mini|-nano)?$"))
    }

    private fun isEncryptedContentSupported(modelId: String): Boolean {
        return isReasoningSupported(modelId)
    }

    private fun getReasoningType(modelId: String): ChatCompletionModel.ReasoningType {
        if (isReasoningRequired(modelId)) {
            return ChatCompletionModel.ReasoningType.REQUIRED
        }
        if (isReasoningSupported(modelId)) {
            return ChatCompletionModel.ReasoningType.OPTIONAL
        }
        return ChatCompletionModel.ReasoningType.NONE
    }

    private fun cleanUp(response: ChatCompletionResponse): ChatCompletionResponse {
        return response.copy(
            content = response.content
                .filter { it !is ChatCompletionResponse.ContentItem.Reasoning }
                .filter { it !is ChatCompletionWebSearch }
        )
    }

    private fun buildParams(request: ChatCompletionRequest): ResponseCreateParams {
        val builder = ResponseCreateParams.builder()
        val (provider, model) = request.modelHandle.split("/", limit = 2)
        if (provider != serviceHandle) {
            throw IllegalArgumentException("Unsupported model provider: $provider")
        }
        if (!isSupportedModelId(model)) {
            throw IllegalArgumentException("Unsupported model: $model")
        }
        builder.store(false)
        builder.model(model)
        if (isTemperatureSupported(model)) {
            builder.temperature(request.temperature)
        }
        builder.maxOutputTokens(request.maxOutputTokens)
        if (isReasoningEffortSupported(model) && request.reasoning.enabled) {
            val reasoningBuilder = Reasoning.builder()
            when (request.reasoning.effort) {
                ChatCompletionRequest.Reasoning.Effort.LOW -> reasoningBuilder.effort(ReasoningEffort.LOW)
                ChatCompletionRequest.Reasoning.Effort.MEDIUM -> reasoningBuilder.effort(ReasoningEffort.MEDIUM)
                ChatCompletionRequest.Reasoning.Effort.HIGH -> reasoningBuilder.effort(ReasoningEffort.HIGH)
                else -> {} // Do nothing
            }
            builder.reasoning(reasoningBuilder.build())
        }
        if (request.instructions.isNotBlank()) {
            builder.instructions(request.instructions)
        }
        builder.tools(buildTools(request.tools, model))
        builder.parallelToolCalls(request.tools.allowParallelUse)
        if (isEncryptedContentSupported(model)) {
            builder.addInclude(ResponseIncludable.REASONING_ENCRYPTED_CONTENT)
        }
        builder.addInclude(ResponseIncludable.WEB_SEARCH_CALL_RESULTS)
        builder.addInclude(ResponseIncludable.WEB_SEARCH_CALL_ACTION_SOURCES)
        builder.toolChoice(
            ResponseCreateParams.ToolChoice.ofOptions(
                when (request.tools.choice) {
                    ChatCompletionRequest.Tools.ToolChoice.AUTO -> ToolChoiceOptions.AUTO
                    ChatCompletionRequest.Tools.ToolChoice.NONE -> ToolChoiceOptions.NONE
                    ChatCompletionRequest.Tools.ToolChoice.REQUIRED -> ToolChoiceOptions.REQUIRED
                }
            )
        )
        builder.inputOfResponse(toSdkResponseInputItems(request.messages))
        return builder.build()
    }

    private fun buildTools(tools: ChatCompletionRequest.Tools, modelId: String): List<Tool> {
        return buildList {
            // Sort by name for deterministic ordering — OpenAI's automatic prompt caching hashes
            // the request prefix, so any reshuffle invalidates the cached prefix.
            tools.functions.sortedBy { it.name }.forEach { tool ->
                this += Tool.ofFunction(
                    FunctionTool.builder()
                        .name(tool.name)
                        .description(tool.description)
                        .parameters(buildFunctionToolParameters(tool.parameters))
                        .strict(false)
                        .build()
                )
            }
            if (tools.external.webSearch.enabled && isWebSearchSupported(modelId)) {
                this += Tool.ofWebSearch(
                    WebSearchTool.builder()
                        .type(WebSearchTool.Type.WEB_SEARCH)
                        .build()
                )
            }
        }
    }

    private fun buildFunctionToolParameters(
        parameters: ParameterValue.ObjectValue?
    ): FunctionTool.Parameters {
        val propsJson = parameters?.properties?.mapValues { JsonValue.fromJsonNode(parameterToJson(it.value)) }
            ?: emptyMap()

        val builder = FunctionTool.Parameters.builder()
            .putAdditionalProperty("type", JsonValue.from("object"))
            .putAdditionalProperty("properties", JsonValue.from(propsJson))

        if (!parameters?.required.isNullOrEmpty()) {
            builder.putAdditionalProperty("required", JsonValue.from(parameters.required))
        }

        return builder.build()
    }

    private fun toSdkResponseInputItems(messages: List<ChatCompletionRequest.Message>): List<ResponseInputItem> {
        val result = mutableListOf<ResponseInputItem>()
        for (message in messages) {
            for (item in message.content) {
                val maybeSdkItem = toSdkResponseItem(item, message.role)
                if (maybeSdkItem != null) {
                    result.add(maybeSdkItem)
                }
            }
        }
        return result
    }

    private fun toSdkResponseItem(
        item: ContentItem,
        messageRole: ChatCompletionRequest.Message.Role
    ): ResponseInputItem? {
        return when (item) {
            is ContentItem.Text -> toSdkResponseItem(item, messageRole)
            is ContentItem.Image -> toSdkResponseItem(item, messageRole)
            is ContentItem.ToolUse -> toSdkResponseItem(item)
            is ContentItem.ToolResult -> toSdkResponseItem(item)
            is ContentItem.Reasoning -> toSdkResponseItem(item)
            is ChatCompletionWebSearch.OpenPage -> toSdkResponseItem(item)
            is ChatCompletionWebSearch.FindInPage -> toSdkResponseItem(item)
            is ChatCompletionWebSearch.Search -> toSdkResponseItem(item)
            is ContentItem.RedactedReasoning -> {
                log.warn("RedactedReasoning content item is not supported in requests to OpenAI, ignoring")
                null
            }
        }
    }

    private fun toSdkResponseItem(
        textItem: ContentItem.Text,
        messageRole: ChatCompletionRequest.Message.Role
    ) = when (messageRole) {
        ChatCompletionRequest.Message.Role.USER -> {
            ResponseInputItem.ofMessage(
                ResponseInputItem.Message.builder()
                    .role(ResponseInputItem.Message.Role.USER)
                    .addInputTextContent(textItem.text)
                    .build()
            )
        }
        ChatCompletionRequest.Message.Role.ASSISTANT -> {
            ResponseInputItem.ofResponseOutputMessage(
                ResponseOutputMessage.builder()
                    .addContent(
                        ResponseOutputText.builder()
                            .text(textItem.text)
                            .build()
                    )
                    .id("msg_aaaaaaaaaaa") // What the heck OpenAI?
                    .status(ResponseOutputMessage.Status.COMPLETED)
                    .build()
            )
        }
    }

    private fun toSdkResponseItem(
        imageItem: ContentItem.Image,
        messageRole: ChatCompletionRequest.Message.Role
    ) = when (messageRole) {
        ChatCompletionRequest.Message.Role.ASSISTANT -> {
            throw IllegalArgumentException(
                "Unsupported content item in assistant message: ${imageItem::class}"
            )
        }
        ChatCompletionRequest.Message.Role.USER -> {
            ResponseInputItem.ofMessage(
                ResponseInputItem.Message.builder()
                    .role(ResponseInputItem.Message.Role.USER)
                    .addContent(
                        ResponseInputImage.builder()
                            .imageUrl(imageItem.toDataUrl())
                            .detail(ResponseInputImage.Detail.AUTO)
                            .build()
                    )
                    .build()
            )
        }
    }

    private fun toSdkResponseItem(toolUseItem: ContentItem.ToolUse): ResponseInputItem {
        return ResponseInputItem.ofFunctionCall(
            ResponseFunctionToolCall.builder()
                .name(toolUseItem.toolUse.name)
                .callId(toolUseItem.toolUse.id)
                .arguments(
                    toolUseInputToJson(toolUseItem.toolUse.input).toString()
                )
                .build()
        )
    }

    private fun toSdkResponseItem(toolResultItem: ContentItem.ToolResult): ResponseInputItem {
        return ResponseInputItem.ofFunctionCallOutput(
            ResponseInputItem.FunctionCallOutput.builder()
                .callId(toolResultItem.toolResult.toolUseId)
                .outputOfResponseFunctionCallOutputItemList(
                    buildList {
                        for (item in toolResultItem.toolResult.output) {
                            when (item) {
                                is ChatCompletionToolUseResult.Output.Text -> {
                                    this += ResponseFunctionCallOutputItem.ofInputText(
                                        ResponseInputTextContent.builder()
                                            .text(item.text)
                                            .build()
                                    )
                                }
                                is ChatCompletionToolUseResult.Output.Image -> {
                                    this += ResponseFunctionCallOutputItem.ofInputImage(
                                        ResponseInputImageContent.builder()
                                            .imageUrl(item.toDataUrl())
                                            .build()
                                    )
                                }
                            }
                        }
                    }
                )
                .build()
        )
    }

    private fun toSdkResponseItem(reasoningItem: ContentItem.Reasoning): ResponseInputItem {
        return ResponseInputItem.ofReasoning(
            ResponseReasoningItem.builder()
                .id(reasoningItem.id ?: error("Reasoning content item must have an id"))
                .encryptedContent(reasoningItem.content)
                .summary(emptyList())
                .build()
        )
    }

    private fun toSdkResponseItem(openPageItem: ChatCompletionWebSearch.OpenPage): ResponseInputItem {
        return ResponseInputItem.ofWebSearchCall(
            ResponseFunctionWebSearch.builder()
                .id(openPageItem.id ?: error("WebSearch OpenPage content item must have an id"))
                .status(ResponseFunctionWebSearch.Status.COMPLETED)
                .action(
                    ResponseFunctionWebSearch.Action.ofOpenPage(
                        ResponseFunctionWebSearch.Action.OpenPage.builder()
                            .url(openPageItem.url)
                            .build()
                    )
                )
                .build()
        )
    }

    private fun toSdkResponseItem(findInPageItem: ChatCompletionWebSearch.FindInPage): ResponseInputItem {
        val find = ResponseFunctionWebSearch.Action.Find.builder()
            .pattern(findInPageItem.pattern)
            .url(findInPageItem.url)
            .type(JsonValue.from("find_in_page")) // SDK bug workaround: https://github.com/openai/openai-java/issues/526
            .build()

        return ResponseInputItem.ofWebSearchCall(
            ResponseFunctionWebSearch.builder()
                .id(findInPageItem.id ?: error("WebSearch FindInPage content item must have an id"))
                .status(ResponseFunctionWebSearch.Status.COMPLETED)
                .action(ResponseFunctionWebSearch.Action.ofFind(find))
                .build()
        )
    }

    private fun toSdkResponseItem(searchItem: ChatCompletionWebSearch.Search): ResponseInputItem {
        return ResponseInputItem.ofWebSearchCall(
            ResponseFunctionWebSearch.builder()
                .id(searchItem.id ?: error("WebSearch Search content item must have an id"))
                .status(ResponseFunctionWebSearch.Status.COMPLETED)
                .action(
                    ResponseFunctionWebSearch.Action.ofSearch(
                        ResponseFunctionWebSearch.Action.Search.builder()
                            .query(searchItem.query)
                            .sources(
                                searchItem.sourceUrls.map { url ->
                                    ResponseFunctionWebSearch.Action.Search.Source.builder().url(url).build()
                                }
                            )
                            .build()
                    )
                )
                .build()
        )
    }

    private fun parseResponse(response: Response) = chatCompletionResponse {
        id = response.id()
        stopReason = when (response.incompleteDetails().getOrNull()?.reason()?.getOrNull()) {
            Response.IncompleteDetails.Reason.MAX_OUTPUT_TOKENS -> ChatCompletionResponse.StopReason.MAX_TOKENS
            null -> ChatCompletionResponse.StopReason.END_TURN
            else -> ChatCompletionResponse.StopReason.UNKNOWN
        }
        content {
            response.output().forEach { item ->
                if (item.isMessage()) {
                    item.asMessage().content().forEach { contentItem ->
                        contentItem.outputText().ifPresent {
                            text(it.text())
                        }
                        contentItem.refusal().ifPresent {
                            text(it.refusal())
                        }
                    }
                } else if (item.isFunctionCall()) {
                    val functionCall = item.asFunctionCall()
                    toolUse {
                        id = functionCall.callId()
                        name = functionCall.name()
                        input {
                            val jsonNode = objectMapper.readTree(functionCall.arguments())
                            parseToolUseInput(jsonNode)
                        }
                    }
                } else if (item.isReasoning()) {
                    val reasoning = item.asReasoning()
                    reasoning {
                        id = reasoning.id()
                        content = reasoning.encryptedContent().getOrNull()
                    }
                } else if (item.isWebSearchCall()) {
                    val webSearchCall = item.asWebSearchCall()
                    val action = webSearchCall.action()
                    if (action.isFind()) {
                        val find = action.asFind()
                        webSearchFindInPage(webSearchCall.id(), find.pattern(), find.url())
                    } else if (action.isOpenPage()) {
                        val open = action.asOpenPage()
                        if (!open.isValid()) {
                            log.warn("Web search open page action is not valid: $open")
                        } else {
                            webSearchOpenPage {
                                id = webSearchCall.id()
                                url = open.url().orElse("")
                                // We do not parse the content of the opened page here, as it is not returned by OpenAI
                            }
                        }
                    } else if (action.isSearch()) {
                        val search = action.asSearch()
                        webSearchSearch(
                            webSearchCall.id(),
                            search.query(),
                            search.sources().getOrNull()?.map { it.url() } ?: emptyList()
                        )
                    } else {
                        val json = action._json().getOrNull()
                        // SDK bug workaround: https://github.com/openai/openai-java/issues/526
                        if (json is JsonObject && json.values["type"]?.asString()?.getOrNull() == "find_in_page") {
                            val pattern = json.values["pattern"]?.asString()?.getOrNull()
                                ?: error("Missing 'pattern' field in find_in_page action")
                            val url = json.values["url"]?.asString()?.getOrNull()
                                ?: error("Missing 'url' field in find_in_page action")
                            webSearchFindInPage(webSearchCall.id(), pattern, url)
                        } else {
                            log.error("Unknown web search action type in OpenAI chat completion response: $action")
                        }
                    }
                } else {
                    log.error("Unknown output item type in OpenAI chat completion response: $item")
                }
            }
        }
        usage {
            val usage = response.usage().getOrNull()
            val totalInput = usage?.inputTokens() ?: 0
            // `inputTokensDetails()` and `cachedTokens()` are `getRequired`-backed in the SDK and
            // throw OpenAIInvalidDataException if absent from the response (e.g. older models or
            // schema drift). We treat that narrowly as "cache info unavailable" and fall back to 0;
            // any other exception is a real bug and must propagate.
            val cachedInput = try {
                usage?.inputTokensDetails()?.cachedTokens() ?: 0
            } catch (e: OpenAIInvalidDataException) {
                log.debug("OpenAI response did not include input_tokens_details.cached_tokens: {}", e.message)
                0
            }
            // OpenAI semantics: inputTokens includes cached tokens. Normalize to the "fresh" portion
            // so that our Usage model stays consistent across providers.
            inputTokens = (totalInput - cachedInput).coerceAtLeast(0)
            outputTokens = usage?.outputTokens() ?: 0
            cacheReadInputTokens = cachedInput
            // OpenAI caching is automatic on the server side — there's no explicit cache-write metric.
            cacheCreationInputTokens = 0
        }
    }

    private fun GenericJsonInputBuilder.parseToolUseInput(jsonNode: JsonNode) {
        when (jsonNode.nodeType) {
            JsonNodeType.BOOLEAN -> boolean(jsonNode.asBoolean())
            JsonNodeType.NUMBER -> number(jsonNode.asDouble())
            JsonNodeType.STRING -> text(jsonNode.asText())
            JsonNodeType.ARRAY -> array {
                for (item in jsonNode) {
                    parseToolUseInput(item)
                }
            }
            JsonNodeType.OBJECT -> objectValue {
                for ((key, value) in jsonNode.properties()) {
                    parseToolUseInput(key, value)
                }
            }
            JsonNodeType.BINARY -> throw IllegalStateException("Unexpected BINARY node in tool use input")
            JsonNodeType.POJO -> throw IllegalStateException("Unexpected POJO node in tool use input")
            JsonNodeType.NULL, JsonNodeType.MISSING -> objectValue { }
        }
    }

    private fun ChatCompletionToolUseInputObjectBuilder.parseToolUseInput(key: String, input: JsonNode) {
        when (input.nodeType) {
            JsonNodeType.BOOLEAN -> boolean(key, input.asBoolean())
            JsonNodeType.NUMBER -> number(key, input.asDouble())
            JsonNodeType.STRING -> text(key, input.asText())
            JsonNodeType.ARRAY -> array(key) {
                for (item in input) {
                    parseToolUseInput(item)
                }
            }
            JsonNodeType.OBJECT -> objectValue(key) {
                for ((childKey, childValue) in input.properties()) {
                    parseToolUseInput(childKey, childValue)
                }
            }
            JsonNodeType.BINARY -> throw IllegalStateException("Unexpected BINARY node in tool use input")
            JsonNodeType.POJO -> throw IllegalStateException("Unexpected POJO node in tool use input")
            JsonNodeType.NULL, JsonNodeType.MISSING -> objectValue(key) { }
        }
    }

    private fun ContentItem.Image.toDataUrl() = toDataUrl(data, mediaType)
    private fun ChatCompletionToolUseResult.Output.Image.toDataUrl() = toDataUrl(data, mediaType)

    private fun toDataUrl(data: ByteArray, mediaType: ChatCompletionImageType): String {
        val mediaTypeString = mediaType.name.lowercase()
        return "data:image/$mediaTypeString;base64,${Base64.getEncoder().encodeToString(data)}"
    }
}