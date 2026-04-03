package space.davids_digital.kiri.agent.engine.lifecycle

/**
 * Marks a method to be called after the agent wakes from sleep, before the first LLM tick.
 *
 * Methods may declare a [space.davids_digital.kiri.agent.frame.FrameBuffer] parameter
 * to receive the current frame buffer (useful for inspecting recent notifications
 * and injecting synthetic tool calls).
 *
 * Can be used on [space.davids_digital.kiri.agent.app.AgentApp] subclasses,
 * [LifecycleHookProvider] implementations, or any other provider scanned by the engine.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class OnAgentWake
