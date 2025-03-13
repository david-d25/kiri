package space.davids_digital.kiri.agent.tool

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

@Component
class AgentToolScanner {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    /**
     * Scans a tool provider for methods annotated with [AgentToolMethod] and registers them in the [AgentToolRegistry].
     *
     * @return the number of methods registered
     */
    fun scan(toolProvider: AgentToolProvider, registry: AgentToolRegistry) = scan(toolProvider, registry, emptyList())

    private fun scan(toolProvider: AgentToolProvider, registry: AgentToolRegistry, basePath: List<String>): Int {
        var count = 0
        val namespaceAnnotation = toolProvider::class.java.getAnnotation(AgentToolNamespace::class.java)
        val namespace = namespaceAnnotation?.value
        if (namespace != null && !validateNamespace(namespace)) {
            log.error("Invalid namespace '$namespace' in provider $toolProvider, skipping")
            return 0
        }
        val subProviders = toolProvider.getSubProviders()
        if (subProviders.isNotEmpty() && namespace.isNullOrBlank()) {
            log.error("Non-empty namespace must be specified for providers with sub-providers, skipping $toolProvider")
            return 0
        }

        val namespacePath = if (namespace.isNullOrBlank()) basePath else basePath + namespace
        for (method in toolProvider::class.functions) {
            val methodAnnotation = method.findAnnotation<AgentToolMethod>() ?: continue
            var methodName = methodAnnotation.name
            if (methodName.isBlank()) {
                methodName = method.name
            }
            val methodDescription = methodAnnotation.description
            if (!validateMethodName(methodName)) {
                log.error("Invalid tool method name '$methodName' in provider $toolProvider, skipping")
                continue
            }
            if (registry.has(namespacePath, methodName)) {
                log.warn("Method '$methodName' in namespace '$namespacePath' is already registered, skipping")
                continue
            }
            registry.put {
                path = namespacePath
                name = methodName
                callable = method
                description = methodDescription
            }
            count++
        }

        for (subProvider in subProviders) {
            count += scan(subProvider, registry, namespacePath)
        }
        return count
    }

    private fun validateNamespace(namespace: String): Boolean {
        return namespace.matches(Regex("[a-zA-Z0-9_]*"))
    }

    private fun validateMethodName(name: String): Boolean {
        return name.matches(Regex("[a-zA-Z0-9_]+"))
    }
}