package space.davids_digital.kiri.agent.app

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolProvider

/**
 * This component manages apps and their lifecycle.
 */
@Component
@AgentToolNamespace("apps")
class AppSystem : AgentToolProvider {
    @AgentToolMethod(name = "list", description = "List all available apps")
    fun listApps(): List<String> {
        TODO("Not yet implemented")
    }

    @AgentToolMethod(name = "open", description = "Open an app by ID")
    fun open(id: String) {
        TODO("Not yet implemented")
    }

    override fun getAvailableAgentToolMethods() = listOf(::listApps, ::open)
}