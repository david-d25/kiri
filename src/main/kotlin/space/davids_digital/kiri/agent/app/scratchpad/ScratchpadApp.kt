package space.davids_digital.kiri.agent.app.scratchpad

import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace

@AgentToolNamespace("scratchpad")
class ScratchpadApp : AgentApp("scratchpad") {
    val content = StringBuilder()

    @AgentToolMethod
    fun append(text: String) {
        content.append(text)
    }

    @AgentToolMethod
    fun clear() {
        content.clear()
    }

    @AgentToolMethod
    fun replace(text: String) {
        content.clear()
        content.append(text)
    }

    override fun render(): List<DataFrame.ContentPart> {
        return dataFrameContent {
            text(content.toString())
        }
    }

    override fun getAvailableAgentToolMethods(): Collection<Function<*>> {
        return listOf(::append, ::clear, ::replace)
    }
}