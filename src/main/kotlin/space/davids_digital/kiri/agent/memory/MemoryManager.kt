package space.davids_digital.kiri.agent.memory

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolProvider

/**
 * This component manages the agent's memory.
 */
@Component
@AgentToolNamespace("memory")
class MemoryManager(
    private val frames: FrameBuffer
) : AgentToolProvider {
    fun tick() {
        TODO()
    }

    override fun getAvailableAgentToolMethods(): Collection<Function<*>> = emptyList()
}