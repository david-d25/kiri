package space.davids_digital.kiri.agent.tool

import space.davids_digital.kiri.llm.LlmMessageRequest

/**
 * Maps agent tool methods to their respective [LlmMessageRequest.Tools.Function].
 */
class AgentToolMapper {
    fun map(function: Function<*>): LlmMessageRequest.Tools.Function {

        return LlmMessageRequest.Tools.Function(
            name = TODO(),
            description = TODO(),
            parameters = TODO()
        )
    }
}