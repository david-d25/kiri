package space.davids_digital.kiri.agent.engine.lifecycle

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.frame.FrameBuffer
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

/**
 * Scans provider objects for lifecycle-annotated methods and invokes them.
 * Parameters are injected by type (e.g. [FrameBuffer]).
 */
@Component
class EngineLifecycleHookExecutor {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun executeOnWake(providers: Iterable<Any>, frames: FrameBuffer) {
        for (provider in providers) {
            for (method in provider::class.functions) {
                if (method.findAnnotation<OnAgentWake>() == null) continue
                try {
                    val args = buildArgs(method, provider, frames)
                    if (method.isSuspend) method.callSuspendBy(args)
                    else method.callBy(args)
                } catch (e: Exception) {
                    log.error(
                        "@OnAgentWake hook failed: {}::{}",
                        provider::class.simpleName, method.name, e
                    )
                }
            }
        }
    }

    private fun buildArgs(
        method: KFunction<*>,
        receiver: Any,
        frames: FrameBuffer
    ): Map<KParameter, Any?> = buildMap {
        for (param in method.parameters) {
            when {
                param.kind == KParameter.Kind.INSTANCE -> put(param, receiver)
                param.type.classifier == FrameBuffer::class -> put(param, frames)
                !param.isOptional -> error(
                    "Unsupported required parameter '${param.name}' of type ${param.type} " +
                    "in @OnAgentWake method ${method.name}"
                )
                else -> log.warn(
                    "Unsupported optional parameter '{}' of type {} in @OnAgentWake method {}; skipping",
                    param.name, param.type, method.name
                )
            }
        }
    }
}
