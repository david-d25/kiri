package space.davids_digital.kiri.agent.tool

import org.springframework.stereotype.Component
import space.davids_digital.kiri.llm.LlmMessageRequest

/**
 * Maps agent tool method parameters their respective [LlmMessageRequest.Tools.Function.ParameterValue].
 */
@Component
class AgentToolParameterMapper {
    fun map(function: Function<*>): LlmMessageRequest.Tools.Function.ParameterValue.ObjectValue {
        TODO()
    }
}