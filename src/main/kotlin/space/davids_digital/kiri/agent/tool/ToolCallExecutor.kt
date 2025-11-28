package space.davids_digital.kiri.agent.tool

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import space.davids_digital.kiri.llm.ChatCompletionToolUse
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf

@Component
class ToolCallExecutor {
    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * Execute a function with the given input and receiver.
     */
    suspend fun execute(function: Function<*>, input: ChatCompletionToolUse.Input, receiver: Any): Any? {
        return try {
            if (function !is KFunction<*>) {
                throw IllegalArgumentException("Function must be a KFunction")
            }

            val args = mutableMapOf<KParameter, Any?>()

            function.instanceParameter?.let { args[it] = receiver }
            function.extensionReceiverParameter?.let { args[it] = receiver }

            val valueParams = function.parameters.filter { it.kind == KParameter.Kind.VALUE }
            if (valueParams.size == 1) {
                val param = valueParams[0]
                val effectiveName = effectiveParameterName(param)
                when (input) {
                    is ChatCompletionToolUse.Input.Object -> {
                        if (input.items.containsKey(effectiveName)) {
                            args[param] = inputToParameter(input.items[effectiveName]!!, param)
                        } else {
                            if (!param.isOptional && !param.type.isMarkedNullable) {
                                throw IllegalArgumentException("Missing required parameter: $effectiveName")
                            }
                        }
                    }
                    else -> {
                        args[param] = inputToParameter(input, param)
                    }
                }
            } else if (valueParams.isNotEmpty()) {
                if (input !is ChatCompletionToolUse.Input.Object) {
                    throw IllegalArgumentException("Function expects multiple parameters, but input is not an object")
                }
                for (param in valueParams) {
                    val paramName = effectiveParameterName(param)
                    if (input.items.containsKey(paramName)) {
                        args[param] = inputToParameter(input.items[paramName]!!, param)
                    } else if (!param.isOptional && !param.type.isMarkedNullable) {
                        throw IllegalArgumentException("Missing required parameter: $paramName")
                    }
                }
            }

            if (function.isSuspend) {
                function.callSuspendBy(args)
            } else {
                function.callBy(args)
            }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            log.error("Error calling function", e)
            throw IllegalArgumentException("Error calling function: ${e.message}", e)
        }
    }

    private fun effectiveParameterName(param: KParameter): String {
        return param.findAnnotation<AgentToolParameter>()?.name?.takeIf { it.isNotBlank() } ?: param.name
        ?: throw IllegalArgumentException("Parameter name is missing")
    }

    private fun inputToParameter(input: ChatCompletionToolUse.Input, parameter: KParameter): Any? {
        return convertInput(input, parameter.type)
    }

    private fun convertInput(input: ChatCompletionToolUse.Input, expectedType: KType): Any? {
        val classifier = expectedType.classifier as? KClass<*>
            ?: throw IllegalArgumentException("Missing type classifier")
        return when (input) {
            is ChatCompletionToolUse.Input.Text -> textToParameter(input.text, classifier)
            is ChatCompletionToolUse.Input.Number -> numberToParameter(input.number, classifier)
            is ChatCompletionToolUse.Input.Boolean -> booleanToParameter(input.boolean, classifier)
            is ChatCompletionToolUse.Input.Array -> convertArray(input, expectedType)
            is ChatCompletionToolUse.Input.Object -> convertObject(input, expectedType)
        }
    }

    private fun convertArray(input: ChatCompletionToolUse.Input.Array, expectedType: KType): Any {
        val classifier = expectedType.classifier as? KClass<*>
            ?: throw IllegalArgumentException("Missing type classifier for array conversion")
        val elementType = expectedType.arguments.firstOrNull()?.type
            ?: throw IllegalArgumentException("Missing element type for array conversion")
        val converted = input.items.map { convertInput(it, elementType) }
        return when (classifier) {
            List::class, MutableList::class -> converted
            Set::class, MutableSet::class -> converted.toSet()
            Array::class -> converted.toTypedArray()
            else -> throw IllegalArgumentException("Unsupported collection type: ${classifier.simpleName}")
        }
    }

    private fun convertObject(input: ChatCompletionToolUse.Input.Object, expectedType: KType): Any? {
        val classifier = expectedType.classifier as? KClass<*>
            ?: throw IllegalArgumentException("Missing type classifier for object conversion")
        return if (Map::class.java.isAssignableFrom(classifier.java)) {
            val valueType = expectedType.arguments.getOrNull(1)?.type
                ?: throw IllegalArgumentException("Missing value type for Map conversion")
            input.items.mapValues { convertInput(it.value, valueType) }
        } else {
            val constructor = classifier.constructors.firstOrNull()
                ?: throw IllegalArgumentException("No constructor found for ${classifier.simpleName}")
            val args = mutableMapOf<KParameter, Any?>()
            for (param in constructor.parameters) {
                val paramName = effectiveParameterName(param)
                val inputValue = input.items[paramName]
                if (inputValue != null) {
                    args[param] = convertInput(inputValue, param.type)
                } else if (!param.isOptional && !param.type.isMarkedNullable) {
                    throw IllegalArgumentException("Missing required parameter: $paramName for ${classifier.simpleName}")
                }
            }
            if (constructor.isSuspend) {
                throw IllegalArgumentException("Suspend constructors are not supported")
            } else {
                constructor.callBy(args)
            }
        }
    }

    private fun textToParameter(text: String, classifier: KClass<*>): Any {
        return when {
            classifier == String::class -> text
            classifier.isSubclassOf(Enum::class) -> {
                val enumConstants = classifier.java.enumConstants as Array<*>
                enumConstants.find { (it as Enum<*>).name == text }
                    ?: throw IllegalArgumentException("Invalid enum value: $text for ${classifier.simpleName}")
            }
            classifier == Int::class ->
                text.toIntOrNull() ?: throw IllegalArgumentException("Cannot convert text to Int: $text")
            classifier == Long::class ->
                text.toLongOrNull() ?: throw IllegalArgumentException("Cannot convert text to Long: $text")
            classifier == Double::class ->
                text.toDoubleOrNull() ?: throw IllegalArgumentException("Cannot convert text to Double: $text")
            classifier == Float::class ->
                text.toFloatOrNull() ?: throw IllegalArgumentException("Cannot convert text to Float: $text")
            classifier == Boolean::class ->
                text.toBoolean()
            else -> throw IllegalArgumentException("Cannot convert text to ${classifier.simpleName}")
        }
    }

    private fun numberToParameter(number: Double, classifier: KClass<*>): Any {
        return when (classifier) {
            Int::class -> number.toInt()
            Long::class -> number.toLong()
            Double::class -> number
            Float::class -> number.toFloat()
            String::class -> number.toString()
            else -> throw IllegalArgumentException("Cannot convert number to ${classifier.simpleName}")
        }
    }

    private fun booleanToParameter(boolean: Boolean, classifier: KClass<*>): Any {
        return when (classifier) {
            Boolean::class -> boolean
            String::class -> boolean.toString()
            else -> throw IllegalArgumentException("Cannot convert boolean to ${classifier.simpleName}")
        }
    }
}