package space.davids_digital.kiri.service

import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.engine.AgentEngine
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.frame.ToolCallFrame
import space.davids_digital.kiri.agent.tool.AgentToolRegistry
import space.davids_digital.kiri.agent.tool.ToolCallExecutor
import space.davids_digital.kiri.llm.ChatCompletionToolUse
import space.davids_digital.kiri.llm.ChatCompletionToolUseResult

@Service
class AdminToolService(
    private val engine: AgentEngine,
    private val toolRegistry: AgentToolRegistry,
    private val toolCallExecutor: ToolCallExecutor,
    private val frameBuffer: FrameBuffer,
) {
    fun listTools(): List<AgentToolRegistry.Entry> {
        engine.refreshToolRegistry()
        return toolRegistry.iterate().toList()
    }

    suspend fun executeTool(toolName: String, input: ChatCompletionToolUse.Input): ToolCallFrame {
        val entry = toolRegistry.find(toolName)
            ?: throw IllegalArgumentException("Tool '$toolName' not found")

        val toolUse = ChatCompletionToolUse(
            id = "admin_${System.currentTimeMillis()}",
            name = toolName,
            input = input
        )

        val result = try {
            toolCallExecutor.execute(entry.callable, input, entry.receiver)
        } catch (e: Exception) {
            val errorResult = ChatCompletionToolUseResult(
                toolUse.id, toolUse.name,
                listOf(ChatCompletionToolUseResult.Output.Text("Tool failed: ${e.message}"))
            )
            return ToolCallFrame(toolUse) { errorResult }.also { frameBuffer.add(it) }
        }

        val toolResult = ToolCallFrame.formatResult(toolUse.id, toolUse.name, result)
        return ToolCallFrame(toolUse) { toolResult }.also { frameBuffer.add(it) }
    }
}
