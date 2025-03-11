package space.davids_digital.kiri.agent.tool

/**
 * This annotation allows components to add a namespace to their methods for the agent tool.
 *
 * The final method name for the agent will be constructed as `$namespace.$methodName`.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AgentToolNamespace(
    val value: String
)
