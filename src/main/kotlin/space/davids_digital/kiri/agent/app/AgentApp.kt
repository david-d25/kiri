package space.davids_digital.kiri.agent.app

abstract class AgentApp(val id: String) {
    abstract fun render(): String
}