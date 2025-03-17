package space.davids_digital.kiri.agent.app

import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.tool.AgentToolProvider

abstract class AgentApp(val id: String): AgentToolProvider {
    abstract fun render(): List<DataFrame.ContentPart>
    fun onOpened() {}
    fun onClose() {}
}