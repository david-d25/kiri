package space.davids_digital.kiri.agent.frame

import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.agent.tool.AgentToolProvider
import space.davids_digital.kiri.llm.ChatCompletionToolUse
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.javaMethod

/**
 * Records real function calls as synthetic [ToolCallFrame]s in the [FrameBuffer],
 * so the LLM sees them as if it made those tool calls itself.
 *
 * Tool name and argument names are derived automatically from [AgentToolNamespace],
 * [AgentToolMethod], and [AgentToolParameter] annotations — no manual specification needed.
 *
 * Usage (bound references):
 * ```
 * frames.trackToolCall(tgApp::switchToChatById, chatId)
 * frames.trackToolCall(tgApp::listLatestMessages, 0, false)
 * ```
 */
class SyntheticToolCallTracker(private val buffer: FrameBuffer) {
    companion object {
        private val callCounter = AtomicLong(0)
    }

    /**
     * Calls [function] with the given positional [args], then records the call and its result
     * as a [ToolCallFrame] in the buffer.
     *
     * [function] must be a bound reference (e.g. `instance::method`) to an [AgentToolMethod]-annotated
     * method on an [AgentToolProvider]. The declaring class is used to resolve [AgentToolNamespace].
     */
    suspend fun <R> call(function: KFunction<R>, vararg args: Any?): R {
        require(function.instanceParameter == null) { "Only bound references are supported" }

        val toolName = resolveToolName(function)
        val valueParams = function.parameters.filter { it.kind == KParameter.Kind.VALUE }
        require(args.size <= valueParams.size) {
            "Too many arguments: expected at most ${valueParams.size}, got ${args.size}"
        }
        val paired = valueParams.zip(args.toList())

        val callArgs = mutableMapOf<KParameter, Any?>()
        val inputItems = mutableMapOf<String, ChatCompletionToolUse.Input>()
        for ((param, value) in paired) {
            callArgs[param] = value
            // Null args are passed to the actual call but omitted from the synthetic input
            // visible to the LLM, since ChatCompletionToolUse.Input has no Null variant.
            if (value != null) {
                val name = param.findAnnotation<AgentToolParameter>()?.name?.takeIf { it.isNotBlank() }
                    ?: param.name ?: "arg${param.index}"
                inputItems[name] = toInput(value)
            }
        }

        @Suppress("UNCHECKED_CAST")
        val result = if (function.isSuspend) {
            function.callSuspendBy(callArgs) as R
        } else {
            function.callBy(callArgs) as R
        }

        val callId = "synthetic_${System.currentTimeMillis()}_${callCounter.getAndIncrement()}"
        val input = ChatCompletionToolUse.Input.Object(inputItems)
        buffer.addToolCall {
            toolUse = ChatCompletionToolUse(callId, toolName, input)
            resultProvider = { ToolCallFrame.formatResult(callId, toolName, result) }
        }

        return result
    }

    private fun resolveToolName(function: KFunction<*>): String {
        val declaringClass = function.javaMethod?.declaringClass
        val namespace = declaringClass?.getAnnotation(AgentToolNamespace::class.java)?.value
        val methodAnnotation = function.findAnnotation<AgentToolMethod>()
        val methodName = methodAnnotation?.name?.takeIf { it.isNotBlank() } ?: function.name
        return if (namespace.isNullOrBlank()) methodName else "${namespace}_$methodName"
    }

    private fun toInput(value: Any): ChatCompletionToolUse.Input = when (value) {
        is String -> ChatCompletionToolUse.Input.Text(value)
        is Number -> ChatCompletionToolUse.Input.Number(value.toDouble())
        is Boolean -> ChatCompletionToolUse.Input.Boolean(value)
        else -> ChatCompletionToolUse.Input.Text(value.toString())
    }

}

/**
 * Calls [function] with the given positional [args] and records the call as a synthetic [ToolCallFrame].
 *
 * Usage:
 * ```
 * frames.trackToolCall(tgApp::switchToChatById, chatId)
 * frames.trackToolCall(tgApp::listLatestMessages, 0, false)
 * ```
 */
suspend fun <R> FrameBuffer.trackToolCall(function: KFunction<R>, vararg args: Any?): R {
    return SyntheticToolCallTracker(this).call(function, *args)
}
