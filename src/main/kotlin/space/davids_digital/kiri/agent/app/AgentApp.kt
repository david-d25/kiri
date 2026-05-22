package space.davids_digital.kiri.agent.app

import space.davids_digital.kiri.agent.tool.AgentToolProvider

abstract class AgentApp(val id: String): AgentToolProvider {
    open suspend fun onOpened() {}
    open suspend fun onClose() {}
}
