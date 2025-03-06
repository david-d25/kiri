package space.davids_digital.kiri.agent.app

abstract class AgentApp(val name: String) {
    abstract fun render(): String
}