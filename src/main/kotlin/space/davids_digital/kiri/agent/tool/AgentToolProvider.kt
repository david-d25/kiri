package space.davids_digital.kiri.agent.tool

/**
 * Implementing this interface is required for components providing methods to be called by agent.
 */
interface AgentToolProvider {
    /**
     * Returns a collection of functions that can be called by the agent.
     *
     * Components implementing this interface should use this method to decide which methods to expose.
     * This method may be called regularly before each agent tick.
     * The exposed methods may be annotated with [AgentToolMethod] to provide additional information.
     *
     * The returned value of the exposed methods will be sent back to the agent.
     * It's recommended for the exposed methods to return either a [String] or [Unit].
     * If a method returns anything other than [String] or [Unit], the return value will be converted to a [String]
     * using [Any.toString].
     */
    fun getAvailableAgentToolMethods(): Collection<Function<*>>
}