package space.davids_digital.kiri.integration.openai

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.ReasoningEffort
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam
import com.openai.models.chat.completions.ChatCompletionContentPart
import com.openai.models.chat.completions.ChatCompletionContentPartImage
import com.openai.models.chat.completions.ChatCompletionContentPartText
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall
import com.openai.models.chat.completions.ChatCompletionMessageToolCall
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam
import com.openai.models.chat.completions.ChatCompletionToolMessageParam
import com.openai.models.chat.completions.ChatCompletionUserMessageParam
import com.openai.models.models.Model
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
import space.davids_digital.kiri.integration.ChatCompletionUtils.toolUseInputToJson
import space.davids_digital.kiri.llm.ChatCompletionRequest
import space.davids_digital.kiri.llm.ChatCompletionResponse
import space.davids_digital.kiri.llm.ChatCompletionToolUseResult
import space.davids_digital.kiri.llm.dsl.GenericJsonInputBuilder
import space.davids_digital.kiri.llm.dsl.LlmToolUseInputObjectBuilder
import space.davids_digital.kiri.llm.dsl.chatCompletionResponse
import space.davids_digital.kiri.model.ChatCompletionModel
import space.davids_digital.kiri.model.ExternalServiceGatewayStatus
import space.davids_digital.kiri.model.Setting
import space.davids_digital.kiri.orm.service.SettingOrmService
import space.davids_digital.kiri.service.ChatCompletionService
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.optionals.getOrNull

@Service
class OpenAIChatCompletionService(
    private val settings: SettingOrmService,
    private val objectMapper: ObjectMapper
) : ChatCompletionService {
    object SettingKeys {
        const val API_KEY = "integration.openai.apiKey"
    }

    override val serviceHandle = "openai"

    class ServiceDisabledException : RuntimeException()

    private val log = LoggerFactory.getLogger(javaClass)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clientRef = AtomicReference<OpenAIClient?>()

    @PostConstruct
    private fun init() {
        scope.launch {
            settings.listen(SettingKeys.API_KEY).collectLatest {
                try {
                    onApiKeyChange(it)
                } catch (e: Exception) {
                    log.error("Failed to handle OpenAI API Key change", e)
                }
            }
        }
    }

    private fun onApiKeyChange(setting: Setting) {
        log.info("Got new OpenAI API key")
        val apiKey = setting.value?.takeIf { it.isNotBlank() }
        if (apiKey == null) {
            log.warn("OpenAI API key is empty, client will be disabled")
            clientRef.set(null)
            return
        }
        clientRef.set(buildClient(apiKey))
        log.info("OpenAI client created")
    }

    private fun buildClient(apiKey: String): OpenAIClient {
        return OpenAIOkHttpClient.builder().apiKey(apiKey).build()
    }

    private fun requireClient() = clientRef.get() ?: throw ServiceDisabledException()

    @EvictCacheOnException(cacheNames = ["OpenAIChatCompletionService#getStatus"])
    override suspend fun request(request: ChatCompletionRequest): ChatCompletionResponse {
        val client = requireClient()
        val params = buildParams(request)
        val response = client.chat().completions().create(params)
        return parseResponse(response)
    }

    @Cacheable(
        value = ["OpenAIChatCompletionService#getStatus"],
        unless = "#result != T(space.davids_digital.kiri.model.ExternalServiceGatewayStatus).READY",
        cacheManager = "oneHour"
    )
    override suspend fun getStatus(): ExternalServiceGatewayStatus {
        val client = clientRef.get() ?: return ExternalServiceGatewayStatus.DISABLED
        return try {
            client.models().list().data()
            ExternalServiceGatewayStatus.READY
        } catch (e: Exception) {
            log.error("Failed to get OpenAI service status", e)
            ExternalServiceGatewayStatus.ERROR
        }
    }

    override suspend fun getModels(): List<ChatCompletionModel> {
        val client = clientRef.get() ?: return emptyList()
        val models = client.models().list().data()
        return models.filter(::isSupportedModel).map {
            ChatCompletionModel(
                handle = serviceHandle + "/" + it.id()
            )
        }
    }

    private fun isSupportedModel(model: Model): Boolean {
        return isSupportedModelId(model.id())
    }

    private fun isSupportedModelId(modelId: String): Boolean {
        return modelId.matches(Regex("^(ft:)?(gpt-4o|gpt-5|gpt-5\\.1|o1|o3|o4)(-chat|-mini|-nano)?$"))
    }

    private fun buildParams(request: ChatCompletionRequest): ChatCompletionCreateParams {
        val builder = ChatCompletionCreateParams.builder()
        val (provider, model) = request.modelHandle.split("/", limit = 2)
        if (provider != serviceHandle) {
            throw IllegalArgumentException("Unsupported model provider: $provider")
        }
        if (!isSupportedModelId(model)) {
            throw IllegalArgumentException("Unsupported model: $model")
        }
        builder.model(model)
        builder.temperature(request.temperature)
        builder.maxCompletionTokens(request.maxOutputTokens)
        val reasoningEffortSupported = model.removePrefix("ft:").startsWith("gpt-5")
                && !model.contains("-chat")
                || model.removePrefix("ft:").startsWith("o")
        if (reasoningEffortSupported) {
            builder.reasoningEffort(ReasoningEffort.MINIMAL)
        }
        if (request.instructions.isNotBlank()) {
            val message = ChatCompletionSystemMessageParam.builder()
                .content(request.instructions)
                .build()
            builder.addMessage(message)
        }
        for (message in request.messages) {
            val (role, content) = message
            when (role) {
                ChatCompletionRequest.Message.Role.USER -> {
                    builder.addMessage(
                        ChatCompletionUserMessageParam.builder()
                            .contentOfArrayOfContentParts(
                                content.mapNotNull { item ->
                                    when (item) {
                                        is ChatCompletionRequest.Message.ContentItem.Text -> {
                                            ChatCompletionContentPart.ofText(
                                                ChatCompletionContentPartText.builder()
                                                    .text(item.text)
                                                    .build()
                                            )
                                        }
                                        is ChatCompletionRequest.Message.ContentItem.Image -> {
                                            ChatCompletionContentPart.ofImageUrl(
                                                ChatCompletionContentPartImage.builder()
                                                    .imageUrl(
                                                        ChatCompletionContentPartImage.ImageUrl.builder()
                                                            .url(item.toDataUrl())
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                        }
                                        else -> {
                                            log.error(
                                                "Skipped unsupported item type for message of role 'user': " +
                                                        item.javaClass.canonicalName
                                            )
                                            null
                                        }
                                    }
                                }
                            )
                            .build()
                    )
                }
                ChatCompletionRequest.Message.Role.ASSISTANT -> {
                    for (item in content) {
                        when (item) {
                            is ChatCompletionRequest.Message.ContentItem.Text -> {
                                builder.addMessage(
                                    ChatCompletionAssistantMessageParam.builder()
                                        .content(item.text)
                                        .build()
                                )
                            }
                            is ChatCompletionRequest.Message.ContentItem.ToolUse -> {
                                builder.addMessage(
                                    ChatCompletionAssistantMessageParam.builder()
                                        .addToolCall(
                                            ChatCompletionMessageToolCall.ofFunction(
                                                ChatCompletionMessageFunctionToolCall.builder()
                                                    .function(
                                                        ChatCompletionMessageFunctionToolCall.Function.builder()
                                                            .name(item.toolUse.name)
                                                            .arguments(
                                                                toolUseInputToJson(item.toolUse.input).toString()
                                                            )
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                        )
                                        .build()
                                )
                            }
                            is ChatCompletionRequest.Message.ContentItem.ToolResult -> {
                                builder.addMessage(
                                    ChatCompletionToolMessageParam.builder()
                                        .toolCallId(item.toolResult.toolUseId)
                                        .content(
                                            when (item.toolResult.output) {
                                                is ChatCompletionToolUseResult.Output.Text -> {
                                                    item.toolResult.output.text
                                                }
                                                is ChatCompletionToolUseResult.Output.Image -> {
                                                    log.error(
                                                        "OpenAI integration does not support tool result images yet"
                                                    )
                                                    "This tool returned an image result, which cannot be displayed."
                                                }
                                            }
                                        )
                                        .build()
                                )
                            }
                            else -> {
                                log.error(
                                    "Skipped unsupported item type for message of role 'assistant': " +
                                            item.javaClass.canonicalName
                                )
                            }
                        }
                    }
                }
            }
        }
        return builder.build()
    }

    private fun parseResponse(response: ChatCompletion) = chatCompletionResponse {
        id = response.id()
        stopReason = when (response.choices().firstOrNull()?.finishReason()) {
            ChatCompletion.Choice.FinishReason.STOP -> ChatCompletionResponse.StopReason.STOP_SEQUENCE
            ChatCompletion.Choice.FinishReason.LENGTH -> ChatCompletionResponse.StopReason.MAX_TOKENS
            ChatCompletion.Choice.FinishReason.TOOL_CALLS -> ChatCompletionResponse.StopReason.TOOL_USE
            ChatCompletion.Choice.FinishReason.FUNCTION_CALL -> ChatCompletionResponse.StopReason.TOOL_USE
            else -> ChatCompletionResponse.StopReason.UNKNOWN
        }
        content {
            response.choices().firstOrNull()?.let { choice ->
                val message = choice.message()
                message.content().ifPresent { content ->
                    text(content)
                }
                message.toolCalls().ifPresent { toolCalls ->
                    for (toolCall in toolCalls) {
                        toolCall.function().ifPresent { function ->
                            toolUse {
                                id = function.function().name()
                                name = function.function().name()
                                input {
                                    val jsonNode = objectMapper.readTree(function.function().arguments())
                                    parseToolUseInput(jsonNode)
                                }
                            }
                        }
                        toolCall.custom().ifPresent {
                            log.error("Got custom tool call in OpenAI chat completion response, which is not supported")
                        }
                    }
                }
                message.audio().ifPresent {
                    log.error("Got audio content in OpenAI chat completion response, which is not supported")
                }
            }
        }
        usage {
            inputTokens = response.usage().getOrNull()?.promptTokens() ?: 0
            outputTokens = response.usage().getOrNull()?.completionTokens() ?: 0
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

    private fun LlmToolUseInputObjectBuilder.parseToolUseInput(key: String, input: JsonNode) {
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

    private fun ChatCompletionRequest.Message.ContentItem.Image.toDataUrl(): String {
        val mediaTypeString = mediaType.name.lowercase()
        return "data:image/$mediaTypeString;base64,${Base64.getEncoder().encodeToString(data)}"
    }
}