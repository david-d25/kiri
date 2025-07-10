package space.davids_digital.kiri.agent.engine

enum class EngineState {
    IDLE,       // Engine is initialized but not running
    RUNNING,    // Engine is actively running ticks
    SLEEPING,   // Engine is in a sleep state (but still considered running)
    ERROR,      // Engine encountered an error
    STOPPING,   // Engine is in the process of stopping
    STOPPED     // Engine has been stopped
}