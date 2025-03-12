package space.davids_digital.kiri.agent.tool

/**
 * This annotation allows components to add a namespace to their methods for the agent tool.
 *
 * For top-level providers, final method name for the agent will be constructed as `$namespace.$methodName`.
 * If the namespace is not specified, the method name will be used as is.
 * If provider has sub-providers, the namespace must be non-empty, and the final method name will be constructed as
 * `$namespace.$subNamespace.$methodName`, and so on.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AgentToolNamespace(
    /**
     * Namespace for agent. Leave blank for no namespace.
     *
     * Namespace should only contain alphanumeric characters and underscores.
     * @see AgentToolScanner.validateNamespace
     */
    val value: String
)
