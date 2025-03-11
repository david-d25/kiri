package space.davids_digital.kiri.agent.tool

/**
 * This annotation allows components to add additional information to their method parameters for the agent tool.
 * Use with methods annotated with [AgentToolMethod].
 * This annotation can also be used with data class fields.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ToolParameter(
    /**
     * Parameter name for agent. Leave blank to use parameter name by default.
     */
    val name: String = "",

    /**
     * Parameter description for agent. Leave blank for no description.
     */
    val description: String = ""
)
