package space.davids_digital.kiri.agent.app

import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.tool.AgentToolProvider

abstract class AgentApp(val id: String): AgentToolProvider {
    abstract suspend fun render(): List<DataFrame.ContentPart>
    open suspend fun onOpened() {}
    open suspend fun onClose() {}
}