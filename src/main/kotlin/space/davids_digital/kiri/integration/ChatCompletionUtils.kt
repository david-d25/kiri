package space.davids_digital.kiri.integration

import com.anthropic.core.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
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

    fun parameterToJson(value: ParameterValue): JsonValue {
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
            is ParameterValue.ArrayValue -> {
                val map = mutableMapOf(
                    "type" to JsonValue.from("array"),
                    "items" to parameterToJson(value.items)
                )
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
}