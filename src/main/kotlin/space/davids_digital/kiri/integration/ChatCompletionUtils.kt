package space.davids_digital.kiri.integration

import com.anthropic.core.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import space.davids_digital.kiri.llm.ChatCompletionRequest
import space.davids_digital.kiri.llm.ChatCompletionRequest.Message.ContentItem
import space.davids_digital.kiri.llm.ChatCompletionRequest.Tools.Function.ParameterValue
import space.davids_digital.kiri.llm.ChatCompletionToolUse
import kotlin.collections.component1
import kotlin.collections.component2

object ChatCompletionUtils {
    fun toolUseInputToJson(toolUse: ChatCompletionToolUse.Input): JsonNode {
        val factory = JsonNodeFactory.instance
        return when (toolUse) {
            is ChatCompletionToolUse.Input.Text -> factory.textNode(toolUse.text)
            is ChatCompletionToolUse.Input.Number -> factory.numberNode(toolUse.number)
            is ChatCompletionToolUse.Input.Boolean -> factory.booleanNode(toolUse.boolean)
            is ChatCompletionToolUse.Input.Array -> factory.arrayNode().apply {
                toolUse.items.forEach { add(toolUseInputToJson(it)) }
            }
            is ChatCompletionToolUse.Input.Object -> factory.objectNode().apply {
                toolUse.items.forEach { (key, value) ->
                    set<JsonNode>(key, toolUseInputToJson(value))
                }
            }
        }
    }

    fun parameterToJson(value: ParameterValue): JsonNode {
        val map = mutableMapOf<String, JsonNode>()
        return when (value) {
            is ParameterValue.ObjectValue -> {
                map["type"] = TextNode("object")
                map["properties"] = ObjectNode(
                    JsonNodeFactory.instance,
                    value.properties.mapValues { parameterToJson(it.value) }
                )
                if (value.required.isNotEmpty()) {
                    map["required"] = ArrayNode(JsonNodeFactory.instance, value.required.map { TextNode(it) })
                }
                if (value.description != null) {
                    map["description"] = TextNode(value.description)
                }
                ObjectNode(JsonNodeFactory.instance, map)
            }
            is ParameterValue.ArrayValue -> {
                map["type"] = TextNode("array")
                map["items"] = parameterToJson(value.items)
                if (value.description != null) {
                    map["description"] = TextNode(value.description)
                }
                ObjectNode(JsonNodeFactory.instance, map)
            }
            is ParameterValue.StringValue -> {
                map["type"] = TextNode("string")
                if (value.description != null) {
                    map["description"] = TextNode(value.description)
                }
                if (value.enum?.isNotEmpty() == true) {
                    map["enum"] = ArrayNode(JsonNodeFactory.instance, value.enum.map { TextNode(it) })
                }
                ObjectNode(JsonNodeFactory.instance, map)
            }
            is ParameterValue.NumberValue -> {
                map["type"] = TextNode("number")
                if (value.description != null) {
                    map["description"] = TextNode(value.description)
                }
                ObjectNode(JsonNodeFactory.instance, map)
            }
            is ParameterValue.BooleanValue -> {
                map["type"] = TextNode("boolean")
                if (value.description != null) {
                    map["description"] = TextNode(value.description)
                }
                ObjectNode(JsonNodeFactory.instance, map)
            }
        }
    }

    fun optimize(request: ChatCompletionRequest): ChatCompletionRequest {
        return request.copy(
            messages = request.messages.map { message ->
                message.copy(
                    content = optimize(message.content)
                )
            }
        )
    }

    fun optimize(contentItems: List<ContentItem>): List<ContentItem> {
        // Merge consecutive text content items, remove empty text items, don't modify other content items
        val optimized = mutableListOf<ContentItem>()
        var buffer = StringBuilder()
        for (item in contentItems) {
            when (item) {
                is ContentItem.Text -> {
                    buffer.append(item.text)
                }
                else -> {
                    // Flush buffer if not empty
                    if (buffer.isNotEmpty()) {
                        optimized.add(ContentItem.Text(buffer.toString()))
                        buffer = StringBuilder()
                    }
                    optimized.add(item)
                }
            }
        }
        // Flush buffer at the end
        if (buffer.isNotEmpty()) {
            optimized.add(ContentItem.Text(buffer.toString()))
        }
        return optimized
    }
}