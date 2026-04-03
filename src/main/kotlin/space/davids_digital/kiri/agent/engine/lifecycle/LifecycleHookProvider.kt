package space.davids_digital.kiri.agent.engine.lifecycle

/**
 * Marker interface for non-app Spring components that contain lifecycle hook methods
 * (e.g. [OnAgentWake]).
 *
 * Implementations are auto-collected by the engine via `List<LifecycleHookProvider>`
 * and scanned for annotated methods alongside opened apps.
 */
interface LifecycleHookProvider
