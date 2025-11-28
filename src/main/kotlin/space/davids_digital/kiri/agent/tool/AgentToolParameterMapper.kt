package space.davids_digital.kiri.agent.tool

import org.springframework.stereotype.Component
import space.davids_digital.kiri.llm.ChatCompletionRequest
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Maps agent tool method parameters their respective [ChatCompletionRequest.Tools.Function.ParameterValue].
 */
@Component
class AgentToolParameterMapper {
    /**
     * Maps a Kotlin function to a parameter value object for an LLM function schema.
     */
    fun map(function: Function<*>): ChatCompletionRequest.Tools.Function.ParameterValue.ObjectValue {
        val kFunction = function as KFunction<*>

        // Get parameters excluding the 'this' parameter for instance methods
        val parameters = kFunction.parameters.filter { it.kind != KParameter.Kind.INSTANCE }

        if (parameters.isEmpty()) {
            // No parameters, return empty object
            return ChatCompletionRequest.Tools.Function.ParameterValue.ObjectValue(
                description = null,
                properties = emptyMap(),
                required = emptyList()
            )
        }

        if (parameters.size == 1) {
            val param = parameters[0]
            val kClass = param.type.classifier as? KClass<*>

            // If it's a data class, process its properties
            if (kClass != null && kClass.isData) {
                return processDataClass(kClass, getParameterDescription(param))
            }
        }

        // Process multiple parameters or a single non-data class parameter
        val properties = mutableMapOf<String, ChatCompletionRequest.Tools.Function.ParameterValue>()
        val required = mutableListOf<String>()

        for (param in parameters) {
            val paramName = getParameterName(param)
            val paramValue = mapType(param.type, getParameterDescription(param))

            properties[paramName] = paramValue

            // If parameter is not optional, add to required list
            if (!param.isOptional && !param.type.isMarkedNullable) {
                required.add(paramName)
            }
        }

        return ChatCompletionRequest.Tools.Function.ParameterValue.ObjectValue(
            description = null,
            properties = properties,
            required = required
        )
    }

    /**
     * Gets the parameter name, either from the [AgentToolParameter] annotation or from the parameter itself.
     */
    private fun getParameterName(parameter: KParameter): String {
        val annotation = parameter.findAnnotation<AgentToolParameter>()
        return if (annotation != null && annotation.name.isNotBlank()) {
            annotation.name
        } else {
            parameter.name ?: "param${parameter.index}"
        }
    }

    /**
     * Gets the parameter description from the [AgentToolParameter] annotation if present.
     */
    private fun getParameterDescription(parameter: KParameter): String? {
        val annotation = parameter.findAnnotation<AgentToolParameter>()
        return if (annotation != null && annotation.description.isNotEmpty()) {
            annotation.description
        } else {
            null
        }
    }

    /**
     * Process a data class and create a parameter object value from its properties.
     */
    private fun processDataClass(
        kClass: KClass<*>,
        description: String?
    ): ChatCompletionRequest.Tools.Function.ParameterValue.ObjectValue {
        val properties = mutableMapOf<String, ChatCompletionRequest.Tools.Function.ParameterValue>()
        val required = mutableListOf<String>()

        // Get primary constructor parameters to determine required properties
        val constructorParams = kClass.primaryConstructor?.parameters ?: emptyList()
        val requiredParamNames = constructorParams
            .filter { !it.isOptional && !it.type.isMarkedNullable }
            .mapNotNull { it.name }
            .toSet()

        // Process all properties of the data class
        for (property in kClass.memberProperties) {
            val propName = getPropertyName(property)
            val propDescription = getPropertyDescription(property)

            properties[propName] = mapType(property.returnType, propDescription)

            if (property.name in requiredParamNames) {
                required.add(propName)
            }
        }

        return ChatCompletionRequest.Tools.Function.ParameterValue.ObjectValue(
            description = description,
            properties = properties,
            required = required
        )
    }

    /**
     * Gets the property name, either from the [AgentToolParameter] annotation or from the property itself.
     */
    private fun getPropertyName(property: kotlin.reflect.KProperty<*>): String {
        val annotation = property.findAnnotation<AgentToolParameter>()
        return if (annotation != null && annotation.name.isNotEmpty()) {
            annotation.name
        } else {
            property.name
        }
    }

    /**
     * Gets the property description from the [AgentToolParameter] annotation if present.
     */
    private fun getPropertyDescription(property: kotlin.reflect.KProperty<*>): String? {
        val annotation = property.findAnnotation<AgentToolParameter>()
        return if (annotation != null && annotation.description.isNotEmpty()) {
            annotation.description
        } else {
            null
        }
    }

    /**
     * Maps a Kotlin type to a parameter value.
     */
    private fun mapType(
        type: KType,
        description: String?
    ): ChatCompletionRequest.Tools.Function.ParameterValue {
        val classifier = type.classifier as? KClass<*> ?: error("Missing type classifier")

        if (classifier == Array::class || classifier.isSubclassOf(Collection::class)) {
            val elementType = type.arguments.firstOrNull()?.type
            val itemSchema = if (elementType != null) {
                mapType(elementType, null)
            } else {
                error("Missing element type for array or collection")
            }
            return ChatCompletionRequest.Tools.Function.ParameterValue.ArrayValue(description, itemSchema)
        }

        return when {
            // Handle primitive types
            classifier == String::class -> createStringValue(description)
            classifier == Int::class
                    || classifier == Long::class
                    || classifier == Float::class
                    || classifier == Double::class ->
                createNumberValue(description)
            classifier == Boolean::class -> createBooleanValue(description)

            // Handle data classes - recursively map their properties
            classifier.isData -> processDataClass(classifier, description)

            // Handle enums - create a string value with valid options
            classifier.isSubclassOf(Enum::class) -> {
                val enumValues = classifier.java.enumConstants?.map { it.toString() } ?: emptyList()
                ChatCompletionRequest.Tools.Function.ParameterValue.StringValue(
                    description = description,
                    enum = enumValues
                )
            }

            // Default to string for other types
            else -> createStringValue(description)
        }
    }

    private fun createStringValue(description: String?): ChatCompletionRequest.Tools.Function.ParameterValue.StringValue {
        return ChatCompletionRequest.Tools.Function.ParameterValue.StringValue(
            description = description,
            enum = null
        )
    }

    private fun createNumberValue(description: String?): ChatCompletionRequest.Tools.Function.ParameterValue.NumberValue {
        return ChatCompletionRequest.Tools.Function.ParameterValue.NumberValue(
            description = description
        )
    }

    private fun createBooleanValue(description: String?): ChatCompletionRequest.Tools.Function.ParameterValue.BooleanValue {
        return ChatCompletionRequest.Tools.Function.ParameterValue.BooleanValue(
            description = description
        )
    }
}