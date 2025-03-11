package space.davids_digital.kiri.agent.tool

/**
 * This annotation allows components to add additional information to their methods for the agent tool.
 * Use with components implementing [AgentToolProvider].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AgentToolMethod(
    /**
     * Method name for agent. Leave blank to use component method name by default.
     */
    val name: String = "",

    /**
     * Description for agent. Leave blank for no description.
     */
    val description: String = "",
)
