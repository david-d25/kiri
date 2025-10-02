package space.davids_digital.kiri.agent.tool

/**
 * This annotation allows components to add additional information to their methods for the agent tool.
 * Use with components implementing [AgentToolProvider].
 * All methods annotated with this annotation should be public.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AgentToolMethod(
    /**
     * Method name for agent. Leave blank to use component method name by default.
     *
     * Name should only contain alphanumeric characters.
     * @see AgentToolScanner.validateMethodName
     */
    val name: String = "",

    /**
     * Description for agent. Leave blank for no description.
     */
    val description: String = "",

    /**
     * Whether to create a [space.davids_digital.kiri.agent.frame.ToolCallFrame] when agent calls this method
     */
    val createFrame: Boolean = true,
)
