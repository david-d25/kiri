package space.davids_digital.kiri.agent.app.scratchpad

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import kotlin.reflect.KFunction

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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

    override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> {
        return listOf(::append, ::clear, ::replace)
    }
}