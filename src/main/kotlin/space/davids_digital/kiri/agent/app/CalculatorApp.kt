package space.davids_digital.kiri.agent.app

import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.tool.AgentToolNamespace

@AgentToolNamespace
class CalculatorApp: AgentApp("calculator") {
    override fun render(): List<DataFrame.ContentPart> {
        TODO("Not yet implemented")
    }

    override fun getAvailableAgentToolMethods(): Collection<Function<*>> {
        TODO("Not yet implemented")
    }
}